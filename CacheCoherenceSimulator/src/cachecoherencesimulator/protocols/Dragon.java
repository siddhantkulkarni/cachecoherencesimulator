/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cachecoherencesimulator.protocols;

import cachecoherencesimulator.*;

/**
 *
 * @author Siddhant Kulkarni
 */
public class Dragon implements ProtocolInterface {

    @Override
    public void localRead(int procID, int blockID, boolean isShared) {
        SimulatorWindow.evaluator.cntMessagesOnBus++;

        if (!Exec.processors[procID].containsBlock(blockID)) {
            //cache read miss
            SimulatorWindow.evaluator.cacheReadMisses++;

            // System.out.println("Read miss proc:"+procID+" block:"+blockID);
            int loc = Exec.processors[procID].getALocationToPlaceBlock();
            if (Exec.processors[procID].localCache[loc].isOccupied && (Exec.processors[procID].localCache[loc].blockState == StateEnum.D || Exec.processors[procID].localCache[loc].blockState == StateEnum.SD)) {
                Exec.ram.values[Exec.processors[procID].localCache[loc].blockID] = Exec.processors[procID].localCache[loc].value;
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
                    Exec.processors[procID].localCache[loc] = new LocalCacheBlock(blockID, StateEnum.SC, temp, 0, true);
                } else {
                    Exec.processors[procID].localCache[loc] = new LocalCacheBlock(blockID, StateEnum.V, temp, 0, true);
                }
            } else {
                 SimulatorWindow.evaluator.cntReadsFromRam++;
                if (isShared) {
                    Exec.processors[procID].localCache[loc] = new LocalCacheBlock(blockID, StateEnum.SC, Exec.ram.values[blockID], 0, true);
                } else {
                    Exec.processors[procID].localCache[loc] = new LocalCacheBlock(blockID, StateEnum.V, Exec.ram.values[blockID], 0, true);
                }
            }

            return;
        }

        LocalCacheBlock tempBlock = Exec.getBlock(procID, blockID);

        switch (tempBlock.blockState) {
            case V:
                if (isShared) {
                    tempBlock.blockState = StateEnum.SC;
                } else {
                    SimulatorWindow.evaluator.cntMessagesOnBus--;
                }
                break;
            case SC:
                if (!isShared) {
                    tempBlock.blockState = StateEnum.V;
                }
                break;
            case D:
                if (isShared) {
                    tempBlock.blockState = StateEnum.SD;
                } else {
                    SimulatorWindow.evaluator.cntMessagesOnBus--;
                }

                break;
            case SD:
                if (!isShared) {
                    tempBlock.blockState = StateEnum.D;
                }
                break;

        }
    }

    @Override
    public void localWrite(int procID, int blockID, boolean isShared) {
        
        SimulatorWindow.evaluator.cntMessagesOnBus+=2;
        if (!Exec.processors[procID].containsBlock(blockID)) {
            //cache write miss
            SimulatorWindow.evaluator.cacheWriteMisses++;
            //SimulatorWindow.evaluator.cntReadsFromRam++;
            int loc = Exec.processors[procID].getALocationToPlaceBlock();
            if (Exec.processors[procID].localCache[loc].isOccupied && (Exec.processors[procID].localCache[loc].blockState == StateEnum.D || Exec.processors[procID].localCache[loc].blockState == StateEnum.SD)) {
                Exec.ram.values[Exec.processors[procID].localCache[loc].blockID] = Exec.processors[procID].localCache[loc].value;
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
                    Exec.processors[procID].localCache[loc] = new LocalCacheBlock(blockID, StateEnum.SD, temp, 0, true);
                } else {
                    Exec.processors[procID].localCache[loc] = new LocalCacheBlock(blockID, StateEnum.D, temp, 0, true);
                }
            } else {
                 SimulatorWindow.evaluator.cntReadsFromRam++;
                if (isShared) {
                    Exec.processors[procID].localCache[loc] = new LocalCacheBlock(blockID, StateEnum.SD, Exec.ram.values[blockID], 0, true);
                } else {
                    Exec.processors[procID].localCache[loc] = new LocalCacheBlock(blockID, StateEnum.D, Exec.ram.values[blockID], 0, true);
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
        LocalCacheBlock tempBlock = Exec.getBlock(procID, blockID);

        switch (tempBlock.blockState) {
            case V:
                tempBlock.blockState=StateEnum.D;
                SimulatorWindow.evaluator.cntMessagesOnBus--;
                break;
            case SC:
                if(isShared){
                    tempBlock.blockState=StateEnum.SD;
                }else{
                    tempBlock.blockState=StateEnum.D;
                }
                    
                break;
            case D:
                if (isShared) {
                    tempBlock.blockState = StateEnum.SD;
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
                    tempBlock.blockState = StateEnum.D;
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
        if (!Exec.processors[procID].containsBlock(blockID)) {
            return;
        }
        LocalCacheBlock tempBlock = Exec.getBlock(procID, blockID);
        switch (tempBlock.blockState) {
            case V:
                tempBlock.blockState = StateEnum.SC;
                break;
            case SC:
                break;
            case D:
                tempBlock.blockState = StateEnum.SD;
                break;
            case SD:
                break;
        }
    }

    @Override
    public void remoteWrite(int procID, int blockID, double value) {
        if (!Exec.processors[procID].containsBlock(blockID)) {
            return;
        }
        LocalCacheBlock tempBlock = Exec.getBlock(procID, blockID);
        switch (tempBlock.blockState) {
            case V:
                tempBlock.blockState = StateEnum.SC;
                break;
            case SC:
                break;
            case D:

                tempBlock.blockState = StateEnum.SC;
                break;
            case SD:
                tempBlock.blockState = StateEnum.SC;
                break;
            //writeback
        }
    }

}
