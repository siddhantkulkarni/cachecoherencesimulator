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
public class MESI implements ProtocolInterface {

    @Override
    public void localRead(int procID, int blockID, boolean isShared) {
        SimulatorWindow.evaluator.cntMessagesOnBus++;

              if (!Exec.processors[procID].containsBlock(blockID)) {// checks if the block is not present in the local cache of the processor
            //cache read miss
            SimulatorWindow.evaluator.cacheReadMisses++;
            SimulatorWindow.evaluator.cntReadsFromRam++;
            int loc = Exec.processors[procID].getALocationToPlaceBlock();// get the new location for the block that is fetched from the main memory
            if (Exec.processors[procID].localCache[loc].isOccupied && Exec.processors[procID].localCache[loc].blockState==StateEnum.M) {//checks if the block is in modify state
                Exec.ram.values[Exec.processors[procID].localCache[loc].blockID] = Exec.processors[procID].localCache[loc].value;// perfrom write back operation
                SimulatorWindow.evaluator.cntWriteBacks++;
            }
            if (!isShared) {
                Exec.processors[procID].localCache[loc] = new LocalCacheBlock(blockID, StateEnum.E, Exec.ram.values[blockID], 0, true);// block will change its state to exclsuive
            } else {
                Exec.processors[procID].localCache[loc] = new LocalCacheBlock(blockID, StateEnum.S, Exec.ram.values[blockID], 0, true);// block state will be shared state
            }

            return;
        }

        LocalCacheBlock tempBlock = Exec.getBlock(procID, blockID);// selects the block from the local cache of the processor

        switch (tempBlock.blockState) {
            case M:

                break;
            case E:
                SimulatorWindow.evaluator.cntMessagesOnBus--;
                break;
            case S:

                break;
            case I:
                
                SimulatorWindow.evaluator.coherenceReadMisses++;
                int loc = Exec.processors[procID].getBlockIndex(blockID);
                Exec.processors[procID].localCache[loc] = new LocalCacheBlock(blockID, StateEnum.S, Exec.ram.values[blockID], 0, true);// block state will be in shared state
                SimulatorWindow.evaluator.cntReadsFromRam++;
                break;
        }
    }

    @Override
    public void localWrite(int procID, int blockID, boolean isShared) {
        SimulatorWindow.evaluator.cntMessagesOnBus++;
        if (!Exec.processors[procID].containsBlock(blockID)) {// checks if the block is not present in the loacl cache of the processor
            //cache write miss
            SimulatorWindow.evaluator.cacheWriteMisses++;
            SimulatorWindow.evaluator.cntReadsFromRam++;
            int loc = Exec.processors[procID].getALocationToPlaceBlock();// get the space to alloacte the new block from the main memory
            if (Exec.processors[procID].localCache[loc].isOccupied && Exec.processors[procID].localCache[loc].blockState==StateEnum.M) {//checks if the block state is in modify state
                Exec.ram.values[Exec.processors[procID].localCache[loc].blockID] = Exec.processors[procID].localCache[loc].value;// perfrom the write back operation
                SimulatorWindow.evaluator.cntWriteBacks++;
            }
            Exec.processors[procID].localCache[loc] = new LocalCacheBlock(blockID, StateEnum.M, Exec.ram.values[blockID], 0, true);// block state will be in modify state and get the block from the main memory
            return;
        }
        LocalCacheBlock tempBlock = Exec.getBlock(procID, blockID);// selects the block from the local cache of the processor

        switch (tempBlock.blockState) {
            case M:
                break;
            case E:
                tempBlock.blockState = StateEnum.M;// block state will be in modify state
                SimulatorWindow.evaluator.cntMessagesOnBus--;
                break;
            case S:
                tempBlock.blockState = StateEnum.M;// block state will be in modify state
                break;
            case I:
                //coherence read miss
                SimulatorWindow.evaluator.coherenceWriteMisses++;
                SimulatorWindow.evaluator.cntReadsFromRam++;
                int loc = Exec.processors[procID].getBlockIndex(blockID);
                Exec.processors[procID].localCache[loc] = new LocalCacheBlock(blockID, StateEnum.M, Exec.ram.values[blockID], 0, true);// block state will be modify state and get the block from the main memory
                break;
        }
    }

    @Override
    public void remoteRead(int procID, int blockID) {
        if (!Exec.processors[procID].containsBlock(blockID)) {// checks if the block is not present in the loacl cache of the processor
            return;
        }
        LocalCacheBlock tempBlock = Exec.getBlock(procID, blockID);// selects the block from the local cache of the processor
        switch (tempBlock.blockState) {
            case M:
                //writeback
                Exec.ram.values[tempBlock.blockID] = tempBlock.value;
                tempBlock.blockState = StateEnum.S;// block state will be in shared state
                SimulatorWindow.evaluator.cntWriteBacks++;
                break;
            case E:
                tempBlock.blockState = StateEnum.S;// block state will be in shared state
                break;
            case S:// block state will be in shared state
                break;
            case I:// block state will be in invalid state
                break;
        }
    }

    @Override
    public void remoteWrite(int procID, int blockID, double value) {
        if (!Exec.processors[procID].containsBlock(blockID)) {// checks if the block is not present in the loacl cache of the processor
            return;
        }
        LocalCacheBlock tempBlock = Exec.getBlock(procID, blockID);// selects the block from the local cache of the processor
        switch (tempBlock.blockState) {
            case M:
                //writeback
                Exec.ram.values[tempBlock.blockID] = tempBlock.value;
                tempBlock.blockState = StateEnum.I;//block state will be in invalid state
                SimulatorWindow.evaluator.entriesToInvalid++;
                SimulatorWindow.evaluator.cntWriteBacks++;
                break;
            case E:
                tempBlock.blockState = StateEnum.I;//block state will be in invalid state
                SimulatorWindow.evaluator.entriesToInvalid++;
                break;
            case S:
                tempBlock.blockState = StateEnum.I;//block state will be in invalid state
                SimulatorWindow.evaluator.entriesToInvalid++;
                break;
            case I://block state will be in invalid state
                break;
        }
    }

}
