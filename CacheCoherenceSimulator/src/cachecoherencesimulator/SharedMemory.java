/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cachecoherencesimulator;

/**
 *
 * @author Team 4 - Siddhant Kulkarni, Ritesh Sangurmath, Ranjan Yadav
 */
public class SharedMemory {
    public int numberOfBlocks;
    public double[] values;

    public SharedMemory(int numberOfBlocks) {
        this.numberOfBlocks = numberOfBlocks;
        values=new double[numberOfBlocks];// create the object based upon the number of the block persent in the main memory
        for(int i=0;i<this.numberOfBlocks;i++){
            values[i]=0;// initial value of the block in the main memory will be assigned to zero
        }
    }
    
}
