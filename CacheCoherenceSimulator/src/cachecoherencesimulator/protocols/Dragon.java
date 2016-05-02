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
public class Dragon implements ProtocolInterface {

    @Override
    public void localRead(int procID, int blockID, boolean isShared) {
        SimulatorWindow.evaluator.cntMessagesOnBus++;

        if (!Exec.processors[procID].containsBlock(blockID)) {// checks if the block is not present in the local cache of the processor
            //cache read miss
            SimulatorWindow.evaluator.cacheReadMisses++;

            // System.out.println("Read miss proc:"+procID+" block:"+blockID);
            int loc = Exec.processors[procID].getALocationToPlaceBlock();// get the new locaction for the block that is fetched from the main memory
            if (Exec.processors[procID].localCache[loc].isOccupied && (Exec.processors[procID].localCache[loc].blockState == StateEnum.D || Exec.processors[procID].localCache[loc].blockState == StateEnum.SD)) {//checks if the block is in dirty state or shared dirty state
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
                    Exec.processors[procID].localCache[loc] = new LocalCacheBlock(blockID, StateEnum.SC, temp, 0, true);// block is in shared-clean state
                } else {
                    Exec.processors[procID].localCache[loc] = new LocalCacheBlock(blockID, StateEnum.V, temp, 0, true);// block is in valid state
                }
            } else {
                 SimulatorWindow.evaluator.cntReadsFromRam++;
                if (isShared) {
                    Exec.processors[procID].localCache[loc] = new LocalCacheBlock(blockID, StateEnum.SC, Exec.ram.values[blockID], 0, true);// block is in shared-clean state and gets the value from the main memory
                } else {
                    Exec.processors[procID].localCache[loc] = new LocalCacheBlock(blockID, StateEnum.V, Exec.ram.values[blockID], 0, true);// block is in valid state and get the value from the main memory
                }
            }

            return;
        }

        LocalCacheBlock tempBlock = Exec.getBlock(procID, blockID);// selects the block from the local cache of the processor

        switch (tempBlock.blockState) {
            case V:
                if (isShared) {
                    tempBlock.blockState = StateEnum.SC;// block will change its state to shared-clean state
                } else {
                    SimulatorWindow.evaluator.cntMessagesOnBus--;// block will be in valid state
                }
                break;
            case SC:
                if (!isShared) {
                    tempBlock.blockState = StateEnum.V;// block will change its state to valid state
                }
                break;
            case D:
                if (isShared) {
                    tempBlock.blockState = StateEnum.SD;// block will change its state to shared-dirty state
                } else {
                    SimulatorWindow.evaluator.cntMessagesOnBus--;// block will be in dirty state
                }

                break;
            case SD:
                if (!isShared) {
                    tempBlock.blockState = StateEnum.D;// block will change its state to dirty state
                }
                break;

        }
    }

    @Override
    public void localWrite(int procID, int blockID, boolean isShared) {
        
        SimulatorWindow.evaluator.cntMessagesOnBus+=2;
        if (!Exec.processors[procID].containsBlock(blockID)) {// checks if the block is not present in the local cache of the processor
            //cache write miss
            SimulatorWindow.evaluator.cacheWriteMisses++;
            //SimulatorWindow.evaluator.cntReadsFromRam++;
            int loc = Exec.processors[procID].getALocationToPlaceBlock();// get the new locaction for the block that is fetched from the main memory
            if (Exec.processors[procID].localCache[loc].isOccupied && (Exec.processors[procID].localCache[loc].blockState == StateEnum.D || Exec.processors[procID].localCache[loc].blockState == StateEnum.SD)) {//checks if the block is in dirty state or shared dirty state
                Exec.ram.values[Exec.processors[procID].localCache[loc].blockID] = Exec.processors[procID].localCache[loc].value;// perfrom the write back operation
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
                    Exec.processors[procID].localCache[loc] = new LocalCacheBlock(blockID, StateEnum.SD, temp, 0, true);// block will be in the shared-dirty state
                } else {
                    Exec.processors[procID].localCache[loc] = new LocalCacheBlock(blockID, StateEnum.D, temp, 0, true);// block will be in dirty state
                }
            } else {
                 SimulatorWindow.evaluator.cntReadsFromRam++;
                if (isShared) {
                    Exec.processors[procID].localCache[loc] = new LocalCacheBlock(blockID, StateEnum.SD, Exec.ram.values[blockID], 0, true);// block will be in shared-dirty state and gets the value from the main memory
                } else {
                    Exec.processors[procID].localCache[loc] = new LocalCacheBlock(blockID, StateEnum.D, Exec.ram.values[blockID], 0, true);// block will be in dirty state and gets the value from the main memory
                }
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
            case V:
                tempBlock.blockState=StateEnum.D;// block change its state to dirty state
                SimulatorWindow.evaluator.cntMessagesOnBus--;
                break;
            case SC:
                if(isShared){
                    tempBlock.blockState=StateEnum.SD;// block will change its state to shared-dirty stste
                }else{
                    tempBlock.blockState=StateEnum.D;// block will change its state to dirty state
                }
                    
                break;
            case D:
                if (isShared) {
                    tempBlock.blockState = StateEnum.SD;// block will change its state to shared-dirty state
                }
                for (int x = 0; x < Exec.processors.length; x++) {
                    for (int z = 0; z < Exec.processors[x].localCache.length; z++) {
                        if (Exec.processors[x].localCache[z].blockID == blockID) {
                            Exec.processors[x].localCache[z].value = Bus.val;
                        }
                    }
                }
                break;
            case SD:
                if (!isShared) {
                    tempBlock.blockState = StateEnum.D;// block will change its state to dirty state
                }
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
        if (!Exec.processors[procID].containsBlock(blockID)) {// checks if the block is not present in the local cache of the processor
            return;
        }
        LocalCacheBlock tempBlock = Exec.getBlock(procID, blockID);// selects the block from the local cache of the processor
        switch (tempBlock.blockState) {
            case V:
                tempBlock.blockState = StateEnum.SC;// block will change its state to shared-clean state
                break;
            case SC:
                break;// block will be in shared-clean
            case D:
                tempBlock.blockState = StateEnum.SD;// block will change its state to shared-dirty state
                break;
            case SD:// block will be in shared-dirty state
                break;
        }
    }

    @Override
    public void remoteWrite(int procID, int blockID, double value) {
        if (!Exec.processors[procID].containsBlock(blockID)) {// checks if the block is not present in the local cache of the processor
            return;
        }
        LocalCacheBlock tempBlock = Exec.getBlock(procID, blockID);// the block is present in the local cache of the processor
        switch (tempBlock.blockState) {
            case V:
                tempBlock.blockState = StateEnum.SC;// block will change its state to shared-clean state
                break;
            case SC:// block will be in shared-clean state
                break;
            case D:

                tempBlock.blockState = StateEnum.SC;// block will change its state to shared-clean state
                break;
            case SD:
                tempBlock.blockState = StateEnum.SC;// block will change its state to shared-clean state
                break;
            //writeback
        }
    }

}
