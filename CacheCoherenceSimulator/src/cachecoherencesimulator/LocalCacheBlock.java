/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cachecoherencesimulator;

/**
 *
 * @author Siddhant Kulkarni
 */
public class LocalCacheBlock{
    public int blockID;
    public StateEnum blockState;
    public double value;
    public int lastUsed;
    public boolean isOccupied;

    public LocalCacheBlock() {
        isOccupied=false;
    }

    public LocalCacheBlock(int blockID, StateEnum blockState, double value, int lastUsed, boolean isOccupied) {
        this.blockID = blockID;
        this.blockState = blockState;
        this.value = value;
        this.lastUsed = lastUsed;
        this.isOccupied = isOccupied;
    }
    
}
