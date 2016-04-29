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
public class SharedMemory {
    public int numberOfBlocks;
    public double[] values;

    public SharedMemory(int numberOfBlocks) {
        this.numberOfBlocks = numberOfBlocks;
        values=new double[numberOfBlocks];
        for(int i=0;i<this.numberOfBlocks;i++){
            values[i]=0;
        }
    }
    
}
