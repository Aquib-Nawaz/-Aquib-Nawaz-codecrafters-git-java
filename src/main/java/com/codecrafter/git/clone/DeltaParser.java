package com.codecrafter.git.clone;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DeltaParser {

    private static final int COPY_OFFSET_BIT_LEN = 4;
    private static final int COPY_SIZE_BIT_LEN = 3;
    public static ObjInfo littleEndian(byte [] in, int offset) {
        byte first = in[offset++];
        int msb = first>>7;
        long len = first&127;
        int iter = 0;

        while(msb!=0){
            iter++;
            byte val = in[offset++];
            msb = val >> 7;
            len += (long)(val&127)<<(iter*7);

        }

        return new ObjInfo(-1,len, iter+1);
    }

    public static ObjInfo parseCopyInstructionLengths(int instruction,int bit_len, byte []fileContent, int offset ){

        int fileOffset = offset;
        long ret = 0;
        for(int bitN=0; bitN<bit_len; bitN++){
            //Little Endian is a bitch
            if((instruction&1) == 1)
                ret |= (long)Byte.toUnsignedInt(fileContent[fileOffset++]) << (8*bitN);
            instruction >>= 1;

        }
        return new ObjInfo(-1, ret, fileOffset-offset);
    }
    public static DeltaObject parse(byte [] fileContent, DeltaObject deltaObject){
        int fileOffset = 0;

        ObjInfo obj = littleEndian(fileContent, 0);
        long srcSize = obj.len;
        fileOffset += obj.offset;

        obj = littleEndian(fileContent, fileOffset);
        long tgtSize = obj.len;
        fileOffset += obj.offset;

        System.out.printf("Src Size:- %d,Tgt Size:- %d\n", srcSize, tgtSize);

        List<DeltaInstruction> instructionsList = new ArrayList<>();

        while(fileOffset < fileContent.length){
            int instruction = Byte.toUnsignedInt(fileContent[fileOffset++]);
            DeltaInstruction instructionObj;
            if((instruction&0x80) != 0){
                //Copy Instruction;
                //Read Offset

                long srcOffset = 0, copySize = 0;

                obj = parseCopyInstructionLengths(instruction, COPY_OFFSET_BIT_LEN, fileContent, fileOffset);
                fileOffset += obj.offset;
                srcOffset = obj.len;

                //Read copySize

                obj = parseCopyInstructionLengths(instruction>>COPY_OFFSET_BIT_LEN, COPY_SIZE_BIT_LEN, fileContent, fileOffset);
                fileOffset += obj.offset;
                copySize = obj.len;
                instructionObj = new DeltaInstruction(2, (int)copySize, (int)srcOffset, null);
//                System.out.printf("Copying %d bytes from offset %d\n", copySize, srcOffset);
            }

            else{
                //Insert Instruction
//                System.out.printf("Insert %d bytes:- %s\n",instruction, new String(fileContent, fileOffset, instruction));
                instructionObj = new DeltaInstruction(1, instruction, 0, Arrays.copyOfRange(fileContent,
                        fileOffset, fileOffset+instruction));
                fileOffset += instruction;
            }

            instructionsList.add(instructionObj);
        }
        deltaObject.setDeltaData(instructionsList, srcSize, tgtSize);
        return deltaObject;
    }

}
