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
public class MESI implements ProtocolInterface {

    @Override
    public void localRead(int procID, int blockID, boolean isShared) {
        SimulatorWindow.evaluator.cntMessagesOnBus++;

              if (!Exec.processors[procID].containsBlock(blockID)) {
            //cache read miss
            SimulatorWindow.evaluator.cacheReadMisses++;
            SimulatorWindow.evaluator.cntReadsFromRam++;
            // System.out.println("Read miss proc:"+procID+" block:"+blockID);
            int loc = Exec.processors[procID].getALocationToPlaceBlock();
            if (Exec.processors[procID].localCache[loc].isOccupied && Exec.processors[procID].localCache[loc].blockState==StateEnum.M) {
                Exec.ram.values[Exec.processors[procID].localCache[loc].blockID] = Exec.processors[procID].localCache[loc].value;
                SimulatorWindow.evaluator.cntWriteBacks++;
            }
            if (!isShared) {
                Exec.processors[procID].localCache[loc] = new LocalCacheBlock(blockID, StateEnum.E, Exec.ram.values[blockID], 0, true);
            } else {
                Exec.processors[procID].localCache[loc] = new LocalCacheBlock(blockID, StateEnum.S, Exec.ram.values[blockID], 0, true);
            }

            return;
        }

        LocalCacheBlock tempBlock = Exec.getBlock(procID, blockID);

        switch (tempBlock.blockState) {
            case M:

                break;
            case E:
                SimulatorWindow.evaluator.cntMessagesOnBus--;
                break;
            case S:

                break;
            case I:
                //coherence read miss
                SimulatorWindow.evaluator.coherenceReadMisses++;
                int loc = Exec.processors[procID].getBlockIndex(blockID);
                Exec.processors[procID].localCache[loc] = new LocalCacheBlock(blockID, StateEnum.S, Exec.ram.values[blockID], 0, true);
                SimulatorWindow.evaluator.cntReadsFromRam++;
                break;
        }
    }

    @Override
    public void localWrite(int procID, int blockID, boolean isShared) {
        SimulatorWindow.evaluator.cntMessagesOnBus++;
        if (!Exec.processors[procID].containsBlock(blockID)) {
            //cache write miss
            SimulatorWindow.evaluator.cacheWriteMisses++;
            SimulatorWindow.evaluator.cntReadsFromRam++;
            int loc = Exec.processors[procID].getALocationToPlaceBlock();
            if (Exec.processors[procID].localCache[loc].isOccupied && Exec.processors[procID].localCache[loc].blockState==StateEnum.M) {
                Exec.ram.values[Exec.processors[procID].localCache[loc].blockID] = Exec.processors[procID].localCache[loc].value;
                SimulatorWindow.evaluator.cntWriteBacks++;
            }
            Exec.processors[procID].localCache[loc] = new LocalCacheBlock(blockID, StateEnum.M, Exec.ram.values[blockID], 0, true);
            return;
        }
        LocalCacheBlock tempBlock = Exec.getBlock(procID, blockID);

        switch (tempBlock.blockState) {
            case M:
                break;
            case E:
                tempBlock.blockState = StateEnum.M;
                SimulatorWindow.evaluator.cntMessagesOnBus--;
                break;
            case S:
                tempBlock.blockState = StateEnum.M;
                break;
            case I:
                //coherence read miss
                SimulatorWindow.evaluator.coherenceWriteMisses++;
                SimulatorWindow.evaluator.cntReadsFromRam++;
                int loc = Exec.processors[procID].getBlockIndex(blockID);
                Exec.processors[procID].localCache[loc] = new LocalCacheBlock(blockID, StateEnum.M, Exec.ram.values[blockID], 0, true);
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
            case M:
                //writeback
                Exec.ram.values[tempBlock.blockID] = tempBlock.value;
                tempBlock.blockState = StateEnum.S;
                SimulatorWindow.evaluator.cntWriteBacks++;
                break;
            case E:
                tempBlock.blockState = StateEnum.S;
                break;
            case S:
                break;
            case I:
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
            case M:
                //writeback
                Exec.ram.values[tempBlock.blockID] = tempBlock.value;
                tempBlock.blockState = StateEnum.I;
                SimulatorWindow.evaluator.entriesToInvalid++;
                SimulatorWindow.evaluator.cntWriteBacks++;
                break;
            case E:
                tempBlock.blockState = StateEnum.I;
                SimulatorWindow.evaluator.entriesToInvalid++;
                break;
            case S:
                tempBlock.blockState = StateEnum.I;
                SimulatorWindow.evaluator.entriesToInvalid++;
                break;
            case I:
                break;
        }
    }

}
