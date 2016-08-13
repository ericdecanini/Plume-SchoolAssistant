package com.pdt.plume;


public class OccurrenceBlock {
    public String[] blocks;
    public String[] blocks_alt;

    public OccurrenceBlock(String periods){
        String[] blocksIn = periods.split(":");

        if (blocksIn[0].equals("0")){
            blocks[0] = "0";
            blocks_alt[0] = "0";
        } else if (blocksIn[0].equals("1")){
            blocks[0] = "1";
            blocks_alt[0] = "0";
        } else if (blocksIn[0].equals("2")){
            blocks[0] = "0";
            blocks_alt[0] = "1";
        } else if (blocksIn[0].equals("3")){
            blocks[0] = "1";
            blocks_alt[0] = "1";
        }

        if (blocksIn[1].equals("0")){
            blocks[1] = "0";
            blocks_alt[1] = "0";
        } else if (blocksIn[1].equals("1")){
            blocks[1] = "1";
            blocks_alt[1] = "0";
        } else if (blocksIn[1].equals("2")){
            blocks[1] = "0";
            blocks_alt[1] = "1";
        } else if (blocksIn[1].equals("3")){
            blocks[1] = "1";
            blocks_alt[1] = "1";
        }

        if (blocksIn[2].equals("0")){
            blocks[2] = "0";
            blocks_alt[2] = "0";
        } else if (blocksIn[2].equals("1")){
            blocks[2] = "1";
            blocks_alt[2] = "0";
        } else if (blocksIn[2].equals("2")){
            blocks[2] = "0";
            blocks_alt[2] = "1";
        } else if (blocksIn[2].equals("3")){
            blocks[2] = "1";
            blocks_alt[2] = "1";
        }

        if (blocksIn[3].equals("0")){
            blocks[3] = "0";
            blocks_alt[3] = "0";
        } else if (blocksIn[3].equals("1")){
            blocks[3] = "1";
            blocks_alt[3] = "0";
        } else if (blocksIn[3].equals("2")){
            blocks[3] = "0";
            blocks_alt[3] = "1";
        } else if (blocksIn[3].equals("3")){
            blocks[3] = "1";
            blocks_alt[3] = "1";
        }
    }
}
