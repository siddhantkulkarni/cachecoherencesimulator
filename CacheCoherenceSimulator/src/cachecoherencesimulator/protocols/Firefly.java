/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cachecoherencesimulator.protocols;

import cachecoherencesimulator.*;

/**
 *
 * @author Team 4 - Siddhant Kulkarni, Ritesh Sangurmath, Ranjan Yadav
 */
public class Firefly implements ProtocolInterface {

    @Override
    public void localRead(int procID, int blockID, boolean isShared) {
        SimulatorWindow.evaluator.cntMessagesOnBus++;

        if (!Exec.processors[procID].containsBlock(blockID)) {// checks if the block is not present in the local cache of the processor
            //cache read miss
            SimulatorWindow.evaluator.cacheReadMisses++;

            // System.out.println("Read miss proc:"+procID+" block:"+blockID);
            int loc = Exec.processors[procID].getALocationToPlaceBlock();// get the new location for the block that is fetched from the main memory
            if (Exec.processors[procID].localCache[loc].isOccupied && (Exec.processors[procID].localCache[loc].blockState == StateEnum.D)) {//checks if the block is in dirty state
                Exec.ram.values[Exec.processors[procID].localCache[loc].blockID] = Exec.processors[procID].localCache[loc].value;// perfrom write back operation
                SimulatorWindow.evaluator.cntWriteBacks++;
            }
            double temp = Double.MIN_VALUE;
            for (int x = 0; x < Exec.processors.length; x++) {
                for (int z = 0; z < Exec.processors[x].localCache.length; z++) {
                    if (Exec.processors[x].localCache[z].blockID == blockID) {
                        temp = Exec.processors[x].localCache[z].value;
                    }
                }
            }
            if (temp != Double.MIN_VALUE) {

                if (isShared) {
                    Exec.processors[procID].localCache[loc] = new LocalCacheBlock(blockID, StateEnum.S, temp, 0, true);// block state will be shared state
                } else {
                    Exec.processors[procID].localCache[loc] = new LocalCacheBlock(blockID, StateEnum.VE, temp, 0, true);// block state will be in valid-exclusive state
                }
            } else {
                SimulatorWindow.evaluator.cntReadsFromRam++;
                // System.out.println("!!");
                if (isShared) {
                    Exec.processors[procID].localCache[loc] = new LocalCacheBlock(blockID, StateEnum.S, Exec.ram.values[blockID], 0, true);// block state will be shared and read the value from the main memory
                } else {
                    Exec.processors[procID].localCache[loc] = new LocalCacheBlock(blockID, StateEnum.VE, Exec.ram.values[blockID], 0, true);// block state will be in valid-exclusive and read the value from the main memory
                }
            }

            return;
        }

        LocalCacheBlock tempBlock = Exec.getBlock(procID, blockID);// selects the block from the local cache of the processor

        switch (tempBlock.blockState) {
            case VE:
                if (isShared) {
                    tempBlock.blockState = StateEnum.S;//block changes its state to shared state
                } else {
                    SimulatorWindow.evaluator.cntMessagesOnBus--;// block will be in valid-exclusive state
                }
                break;
            case S:
                if (!isShared) {
                    tempBlock.blockState = StateEnum.VE;// block will be in valid-exclusive state
                }
                break;
            case D:// block will be in dirty state
                break;
        }
    }

    @Override
    public void localWrite(int procID, int blockID, boolean isShared) {
        SimulatorWindow.evaluator.cntMessagesOnBus+=2;
        if (!Exec.processors[procID].containsBlock(blockID)) {// checks if the block is not present in the local cache of the processor
            //cache write miss
            SimulatorWindow.evaluator.cacheWriteMisses++;
           // SimulatorWindow.evaluator.cntReadsFromRam++;
            int loc = Exec.processors[procID].getALocationToPlaceBlock();// get the new location for the block that is fetched from the main memory
            if (Exec.processors[procID].localCache[loc].isOccupied && (Exec.processors[procID].localCache[loc].blockState == StateEnum.D)) {//checks if the block is in dirty state
                Exec.ram.values[Exec.processors[procID].localCache[loc].blockID] = Exec.processors[procID].localCache[loc].value;//perfrom the write back operation
                SimulatorWindow.evaluator.cntWriteBacks++;
            }
            double temp = Double.MIN_VALUE;
            for (int x = 0; x < Exec.processors.length; x++) {
                for (int z = 0; z < Exec.processors[x].localCache.length; z++) {
                    if (Exec.processors[x].localCache[z].blockID == blockID) {
                        temp = Exec.processors[x].localCache[z].value;
                    }
                }
            }
            if (temp != Double.MIN_VALUE) {

                Exec.processors[procID].localCache[loc] = new LocalCacheBlock(blockID, StateEnum.D, temp, 0, true);// block will be in diry state

            } else {
                SimulatorWindow.evaluator.cntReadsFromRam++;
                Exec.processors[procID].localCache[loc] = new LocalCacheBlock(blockID, StateEnum.D, Exec.ram.values[blockID], 0, true);// block will be in dirty state and get the value from the main memory

            }

            for (int x = 0; x < Exec.processors.length; x++) {
                for (int z = 0; z < Exec.processors[x].localCache.length; z++) {
                    if (Exec.processors[x].localCache[z].blockID == blockID) {
                        Exec.processors[x].localCache[z].value = Bus.val;
                    }
                }
            }
            return;
        }
        LocalCacheBlock tempBlock = Exec.getBlock(procID, blockID);// selects the block from the local cache of the processor

        switch (tempBlock.blockState) {
            case VE:
                tempBlock.blockState = StateEnum.D;// block will change its state to dirty state
                SimulatorWindow.evaluator.cntMessagesOnBus--;
                break;
            case S:
                tempBlock.blockState = StateEnum.D;// block wll change its state to dirty
                break;
            case D:// block will be in dirty state

                for (int x = 0; x < Exec.processors.length; x++) {
                    for (int z = 0; z < Exec.processors[x].localCache.length; z++) {
                        if (Exec.processors[x].localCache[z].blockID == blockID) {
                            Exec.processors[x].localCache[z].value = Bus.val;
                        }
                    }
                }
                break;

        }
    }

    @Override
    public void remoteRead(int procID, int blockID) {
        if (!Exec.processors[procID].containsBlock(blockID)) {// check if the block is not present in the local cache of the processor
            return;
        }
        LocalCacheBlock tempBlock = Exec.getBlock(procID, blockID);// selects the block from the loacl cache of the processor
        switch (tempBlock.blockState) {
            case VE:
                tempBlock.blockState = StateEnum.S;// block will change its state to shared state
                break;
            case S:// block will be in shared state
                break;
            case D:
                tempBlock.blockState = StateEnum.S;// block will change its state to shared state
                break;

        }
    }

    @Override
    public void remoteWrite(int procID, int blockID, double value) {
        if (!Exec.processors[procID].containsBlock(blockID)) {// check if the block is not present in the local cache of the processor
            return;
        }
        LocalCacheBlock tempBlock = Exec.getBlock(procID, blockID);//selects the block from the local cache of the processor
        switch (tempBlock.blockState) {
            case VE:
                tempBlock.blockState = StateEnum.S;// block will change its state to shared state
                break;
            case S:// block will be in shared state
                break;
            case D:
                tempBlock.blockState = StateEnum.S;// block will change its state to shared state
                break;

        }
    }

}
