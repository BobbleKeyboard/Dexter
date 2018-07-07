package com.bobble.dexter;


import com.bobble.dexter.core.Dexter;
import com.bobble.dexter.models.ClassDefItem;
import com.bobble.dexter.models.FieldIdItem;
import com.bobble.dexter.models.MethodIdItem;
import com.bobble.dexter.models.ProtoIdItem;
import com.bobble.dexter.models.TypeIdItem;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.gradle.internal.impldep.org.apache.http.util.TextUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;


public class DexterDefaultTask extends DefaultTask {

    private byte[] buffer = new byte[4096];
    private ZipInputStream zipInputStream;
    private RandomAccessFile randomAccessFileOfDex;
    private static final byte[] MAGIC_VALUE = new byte[]{0x64, 0x65, 0x78, 0x0a, 0x30, 0x33, 0x35, 0x00};
    private boolean isBigEndian;
    private byte tmpBuf[] = new byte[4];
    private int endianTag, fileSize, headerSize, stringIdsSize, stringIdsOff, typeIdsSize,
            typeIdsOff, protoIdsSize, protoIdsOff, fieldIdsSize, fieldIdsOff, methodIdsSize,
            methodIdsOff, classDefsSize, classDefsOff;
    private static final int ENDIAN_CONSTANT = 0x12345678;
    private static final int REVERSE_ENDIAN_CONSTANT = 0x78563412;
    private String[] mStrings;
    private TypeIdItem[] mTypeIds;
    private ProtoIdItem[] mProtoIds;
    private FieldIdItem[] mFieldIds;
    private MethodIdItem[] mMethodIds;
    private ClassDefItem[] mClassDefs;
    private String descriptor;
    private PrintWriter printWriter;
    private File classNameFile;
    private String projectPath, apkOutputPath, debugApkPath, releaseApkPath, outputPath;


    @TaskAction
    public void dexterTask() {
        projectPath = getProject().getRootDir().getAbsolutePath();

        Dexter.BuildVariant variant = Dexter.configure().getVariant();
        String apkPath;
        apkOutputPath = projectPath + File.separator + "/app/build/outputs/apk/";
        if (variant != null) {
            switch (variant) {
                case DEBUG: {
                    apkPath = apkOutputPath + "debug/app-debug.apk";
                    break;
                }
                case RELEASE: {
                    if (Dexter.configure().isSigned()) {
                        apkPath = apkOutputPath + "release/app-release.apk";
                    } else {
                        apkPath = apkOutputPath + "release/app-release-unsigned.apk";
                    }
                    break;
                }
                default: {
                    apkPath = apkOutputPath + "debug/app-debug.apk";
                }
            }
        } else {
            apkPath = apkOutputPath + "debug/";
        }

        String userApkPath = Dexter.configure().getApkPath();
        if (userApkPath != null && !userApkPath.isEmpty()) {
            apkPath = userApkPath;
        }

        outputPath = projectPath + File.separator + "/app/build/outputs/dexter/";
        File dexterDir = new File(outputPath);
        if (!dexterDir.exists()) {
            dexterDir.mkdirs();
        }

        try {
            File file = new File(apkPath);
            FileInputStream fileInputStream = new FileInputStream(file);
            zipInputStream = new ZipInputStream(new BufferedInputStream(fileInputStream));
            extractZipEntriesFromZip(zipInputStream)
                    .concatMapIterable(x -> x)
                    .subscribe(new Observer<File>() {

                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onNext(File file) {
                            randomAccessFileOfDex = createRandomAccessFile(file);
                            try {
                                if (randomAccessFileOfDex != null) {
                                    parseAndVerifyHeader(randomAccessFileOfDex);
                                    loadStrings();
                                    loadTypeIds();
                                    loadProtoIds();
                                    loadFieldIds();
                                    loadMethodIds();
                                    loadClassDefs(file.getName());
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onError(Throwable e) {

                        }

                        @Override
                        public void onComplete() {

                        }
                    });

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    /**
     * <p>Function which parses the header of a DEX file and verifies it as DEX through Magic
     * number, check for endianness and assign sizes for string, method and classes.</p>
     *
     * @param randomAccessFileOfDex File to be parsed
     * @throws IOException in case if File is not readable.
     */
    private void parseAndVerifyHeader(RandomAccessFile randomAccessFileOfDex) throws IOException {
        randomAccessFileOfDex.seek(0);
        byte[] magic = new byte[8];

        //verify magic number
        readBytes(magic, randomAccessFileOfDex);
        StringBuilder sb = new StringBuilder();
        for (byte b : magic) {
            sb.append(String.format("%02X ", b));
        }
        if (!verifyMagic(magic)) {
            System.err.println("Magic number is wrong -- are you sure " +
                    "this is a DEX file?");
            throw new DexDataException();
        }

        randomAccessFileOfDex.seek(8 + 4 + 20 + 4 + 4);
        endianTag = readInt(randomAccessFileOfDex);
        if (endianTag == ENDIAN_CONSTANT) {
            /* Do Nothing*/
        } else if (endianTag == REVERSE_ENDIAN_CONSTANT) {
            isBigEndian = true;
        } else {
            System.err.println("Endian constant has unexpected value " +
                    Integer.toHexString(endianTag));
            throw new DexDataException();
        }


        randomAccessFileOfDex.seek(8 + 4 + 20);  // magic, checksum, signature
        fileSize = readInt(randomAccessFileOfDex);
        headerSize = readInt(randomAccessFileOfDex);
        /*mHeaderItem.endianTag =*/
        readInt(randomAccessFileOfDex);
        /*mHeaderItem.linkSize =*/
        readInt(randomAccessFileOfDex);
        /*mHeaderItem.linkOff =*/
        readInt(randomAccessFileOfDex);
        /*mHeaderItem.mapOff =*/
        readInt(randomAccessFileOfDex);
        stringIdsSize = readInt(randomAccessFileOfDex);
        stringIdsOff = readInt(randomAccessFileOfDex);
        typeIdsSize = readInt(randomAccessFileOfDex);
        typeIdsOff = readInt(randomAccessFileOfDex);
        protoIdsSize = readInt(randomAccessFileOfDex);
        protoIdsOff = readInt(randomAccessFileOfDex);
        fieldIdsSize = readInt(randomAccessFileOfDex);
        fieldIdsOff = readInt(randomAccessFileOfDex);
        methodIdsSize = readInt(randomAccessFileOfDex);
        methodIdsOff = readInt(randomAccessFileOfDex);
        classDefsSize = readInt(randomAccessFileOfDex);
        classDefsOff = readInt(randomAccessFileOfDex);
        /*mHeaderItem.dataSize =*/
        readInt(randomAccessFileOfDex);
        /*mHeaderItem.dataOff =*/
        readInt(randomAccessFileOfDex);
    }

    /**
     * <p>Reading bytes from a random file given as argument.</p>
     *
     * @param buffer           read given bytes.
     * @param randomAccessFile Random file to be used.
     * @throws IOException In case if data is not read from file successfully.
     */
    private void readBytes(byte[] buffer, RandomAccessFile randomAccessFile) throws IOException {
        randomAccessFile.readFully(buffer);
    }

    /**
     * Reads a signed 32-bit integer, byte-swapping if necessary.
     */
    private int readInt(RandomAccessFile randomAccessFile) throws IOException {
        randomAccessFile.readFully(tmpBuf, 0, 4);

        if (isBigEndian) {
            return (tmpBuf[3] & 0xff) | ((tmpBuf[2] & 0xff) << 8) |
                    ((tmpBuf[1] & 0xff) << 16) | ((tmpBuf[0] & 0xff) << 24);
        } else {
            return (tmpBuf[0] & 0xff) | ((tmpBuf[1] & 0xff) << 8) |
                    ((tmpBuf[2] & 0xff) << 16) | ((tmpBuf[3] & 0xff) << 24);
        }
    }

    /**
     * <p>Check for equality of magic number with given bytes.</p>
     *
     * @param magic bytes to be compared.
     * @return true when match otherwise false.
     */
    private static boolean verifyMagic(byte[] magic) {
        return Arrays.equals(magic, MAGIC_VALUE);
    }

    /**
     * Create Random access file from a given file.
     *
     * @param file File to be converted
     * @return Random access file created in read mode only
     */
    private RandomAccessFile createRandomAccessFile(File file) {
        try {
            return new RandomAccessFile(file, "r");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Getting an observable stream of dex file from Zip file as input.
     *
     * @param zipInputStream zip inputstream of APK.
     * @return Observable stream of array of DexFiles.
     * @throws IOException
     */
    private Observable<List<File>> extractZipEntriesFromZip(ZipInputStream zipInputStream) throws IOException {
        List<File> dexFileList = new ArrayList<>();
        ZipEntry zipEntry = zipInputStream.getNextEntry();
        while (zipEntry != null) {
            if (zipEntry.getName().matches("classes.*\\.dex")) {
                File tempFile = new File(outputPath + zipEntry.getName());
                try {
                    int size;
                    FileOutputStream fileOutputStream = new FileOutputStream(tempFile);
                    while ((size = zipInputStream.read(buffer)) > 0) {
                        fileOutputStream.write(buffer, 0, size);
                    }
                    dexFileList.add(tempFile);
                    fileOutputStream.close();
                } catch (Exception e) {
                    System.out.println("File temp not made successfully");
                }
            }
            zipEntry = zipInputStream.getNextEntry();
        }
        return Observable.just(dexFileList);
    }

    /**
     * Loads the string table out of the DEX.
     * <p>
     * First we read all of the string_id_items, then we read all of the
     * string_data_item.  Doing it this way should allow us to avoid
     * seeking around in the file.
     */
    private void loadStrings() throws IOException {
        int count = stringIdsSize;
        int stringOffsets[] = new int[count];

        System.out.println("reading " + count + " strings");

        randomAccessFileOfDex.seek(stringIdsOff);
        for (int i = 0; i < count; i++) {
            stringOffsets[i] = readInt(randomAccessFileOfDex);
        }

        mStrings = new String[count];

        randomAccessFileOfDex.seek(stringOffsets[0]);
        for (int i = 0; i < count; i++) {
            randomAccessFileOfDex.seek(stringOffsets[i]);         // should be a no-op
            mStrings[i] = readString(randomAccessFileOfDex);
            //System.out.println("STR: " + i + ": " + mStrings[i]);
        }
    }

    /**
     * Loads the type ID list.
     */
    private void loadTypeIds() throws IOException {
        int count = typeIdsSize;
        mTypeIds = new TypeIdItem[count];

        System.out.println("reading " + count + " typeIds");
        randomAccessFileOfDex.seek(typeIdsOff);
        for (int i = 0; i < count; i++) {
            mTypeIds[i] = new TypeIdItem();
            mTypeIds[i].setDescriptorIdx(readInt(randomAccessFileOfDex));

//            System.out.println(i + ": " + mTypeIds[i].getDescriptorIdx() +
//                " " + mStrings[mTypeIds[i].getDescriptorIdx()]);
        }
    }

    /**
     * Loads the field ID list.
     */
    private void loadFieldIds() throws IOException {
        int count = fieldIdsSize;
        mFieldIds = new FieldIdItem[count];

        //System.out.println("reading " + count + " fieldIds");
        randomAccessFileOfDex.seek(fieldIdsOff);
        for (int i = 0; i < count; i++) {
            mFieldIds[i] = new FieldIdItem();
            mFieldIds[i].setClassIdx((short) (readShort(randomAccessFileOfDex) & 0xffff));
            mFieldIds[i].setTypeIdx((short) (readShort(randomAccessFileOfDex) & 0xffff));
            mFieldIds[i].setNameIdx(readInt(randomAccessFileOfDex));

//            System.out.println(i + ": " + mFieldIds[i].getNameIdx() +
//                " " + mStrings[mFieldIds[i].getNameIdx()]);
        }
    }

    /**
     * Loads the proto ID list.
     */
    private void loadProtoIds() throws IOException {
        int count = protoIdsSize;
        mProtoIds = new ProtoIdItem[count];

        //System.out.println("reading " + count + " protoIds");
        randomAccessFileOfDex.seek(protoIdsOff);

        /*
         * Read the proto ID items.
         */
        for (int i = 0; i < count; i++) {
            mProtoIds[i] = new ProtoIdItem();
            mProtoIds[i].setShortyIdx(readInt(randomAccessFileOfDex));
            mProtoIds[i].setReturnTypeIdx(readInt(randomAccessFileOfDex));
            mProtoIds[i].setParametersOff(readInt(randomAccessFileOfDex));

//            System.out.println(i + ": " + mProtoIds[i].getShortyIdx() +
//                " " + mStrings[mProtoIds[i].getShortyIdx()]);
        }

        /*
         * Go back through and read the type lists.
         */
        for (int i = 0; i < count; i++) {
            ProtoIdItem protoId = mProtoIds[i];

            int offset = protoId.getParametersOff();

            if (offset == 0) {
                //protoId.types = new int[0];
                continue;
            } else {
                randomAccessFileOfDex.seek(offset);
                int size = readInt(randomAccessFileOfDex);       // #of entries in list
                //protoId.types = new int[size];

                for (int j = 0; j < size; j++) {
                    readShort(randomAccessFileOfDex);
                }
            }
        }
    }

    /**
     * Loads the method ID list.
     */
    private void loadMethodIds() throws IOException {
        int count = methodIdsSize;
        mMethodIds = new MethodIdItem[count];

        //System.out.println("reading " + count + " methodIds");
        randomAccessFileOfDex.seek(methodIdsOff);
        for (int i = 0; i < count; i++) {
            mMethodIds[i] = new MethodIdItem();
            mMethodIds[i].setClassIdx((short) (readShort(randomAccessFileOfDex) & 0xffff));
            mMethodIds[i].setProtoIdx((short) (readShort(randomAccessFileOfDex) & 0xffff));
            mMethodIds[i].setNameIdx(readInt(randomAccessFileOfDex));

//            System.out.println(i + ": " + mMethodIds[i].getNameIdx() +
//                " " + mStrings[mMethodIds[i].getNameIdx()]);
        }
    }

    /**
     * Loads the class defs list.
     */
    private void loadClassDefs(String dexFileName) throws IOException {
        int count = classDefsSize;
        printWriter = null;
        mClassDefs = new ClassDefItem[count];
        classNameFile = new File(outputPath + "DexClasses of " + dexFileName + ".txt");
        classNameFile.createNewFile();
        System.out.println("reading " + count + " classDefs");
        randomAccessFileOfDex.seek(classDefsOff);
        for (int i = 0; i < count; i++) {
            mClassDefs[i] = new ClassDefItem();
            mClassDefs[i].setClassIdx(readInt(randomAccessFileOfDex));

            /* access_flags = */
            readInt(randomAccessFileOfDex);
            /* superclass_idx = */
            readInt(randomAccessFileOfDex);
            /* interfaces_off = */
            readInt(randomAccessFileOfDex);
            /* source_file_idx = */
            readInt(randomAccessFileOfDex);
            /* annotations_off = */
            readInt(randomAccessFileOfDex);
            /* class_data_off = */
            readInt(randomAccessFileOfDex);
            /* static_values_off = */
            readInt(randomAccessFileOfDex);

//            System.out.println(i + ": " + mClassDefs[i].getClassIdx() + " " +
//                mStrings[mTypeIds[mClassDefs[i].getClassIdx()].getDescriptorIdx()]);
            descriptor = mStrings[mTypeIds[mClassDefs[i].getClassIdx()].getDescriptorIdx()];
            //System.out.println(formatAndPrintDescriptor(descriptor));
            dumpDescriptorToFile(formatAndPrintDescriptor(descriptor), classNameFile);
        }
        if (printWriter != null) {
            printWriter.close();
        }
    }

    /**
     * Dump class description to files.
     *
     * @param descriptor descriptor of class files.
     * @param dumpFile   File in which text has to be dumped.
     */
    private void dumpDescriptorToFile(String descriptor, File dumpFile) {
        if (printWriter == null) {
            try {
                printWriter = new PrintWriter(dumpFile);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        printWriter.println(descriptor);
        printWriter.flush();
    }

    /**
     * Formatter for class descriptor
     *
     * @param descriptor descriptor of the class from Dex file.
     * @return formatted for class name.
     */
    private String formatAndPrintDescriptor(String descriptor) {
        String formattedDescriptor = descriptor
                //Strip of L
                .substring(1)
                //Strip all '/' in '.'
                .replace('/', '.');
        formattedDescriptor = formattedDescriptor.substring(0, formattedDescriptor.length() - 1);
        return formattedDescriptor;
    }


    /**
     * Reading string from a Random Access File.
     *
     * @param randomAccessFile file to be read.
     * @return string read from file.
     * @throws IOException
     */
    private String readString(RandomAccessFile randomAccessFile) throws IOException {
        int utf16len = readUnsignedLeb128();
        byte inBuf[] = new byte[utf16len * 3];      // worst case
        int idx;

        for (idx = 0; idx < inBuf.length; idx++) {
            byte val = readByte(randomAccessFile);
            if (val == 0)
                break;
            inBuf[idx] = val;
        }

        return new String(inBuf, 0, idx, "UTF-8");
    }

    /**
     * Reads a signed 16-bit integer, byte-swapping if necessary.
     */
    private short readShort(RandomAccessFile randomAccessFile) throws IOException {
        randomAccessFile.readFully(tmpBuf, 0, 2);
        if (isBigEndian) {
            return (short) ((tmpBuf[1] & 0xff) | ((tmpBuf[0] & 0xff) << 8));
        } else {
            return (short) ((tmpBuf[0] & 0xff) | ((tmpBuf[1] & 0xff) << 8));
        }
    }

    private int readUnsignedLeb128() throws IOException {
        int result = 0;
        byte val;

        do {
            val = readByte(randomAccessFileOfDex);
            result = (result << 7) | (val & 0x7f);
        } while (val < 0);

        return result;
    }

    /**
     * Reading bytes from a file.
     *
     * @param randomAccessFile
     * @return
     * @throws IOException
     */
    private byte readByte(RandomAccessFile randomAccessFile) throws IOException {
        randomAccessFile.readFully(tmpBuf, 0, 1);
        return tmpBuf[0];
    }


}
