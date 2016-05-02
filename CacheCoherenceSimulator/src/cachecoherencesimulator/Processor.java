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

public class Processor {
    
    public LocalCacheBlock[] localCache;

    public Processor(int cntBlocks) {
        this.localCache=new LocalCacheBlock[cntBlocks];// create the local cache object based upon the number of the block entered for the local cache
        for(int i=0;i<cntBlocks;i++){
            this.localCache[i]=new LocalCacheBlock();// assign each block as not occupied
            this.localCache[i].blockID=-1;
            
        }
    }
    public int getALocationToPlaceBlock(){ //get location to place block in local cache
        int lruIndex=-1,lruVal=-1;
        for(int i=0;i<this.localCache.length;i++){
            if(!this.localCache[i].isOccupied)// checks if the block is occupied
                return i;
            
            if(lruVal<this.localCache[i].lastUsed){// checks the last used of each block
                lruIndex=i;
                lruVal=this.localCache[i].lastUsed;
            }
        }
        
        return lruIndex;
    }
    public boolean containsBlock(int block){// checks if the block is present in the processor's local cache
        for(int i=0;i<localCache.length;i++){
            if(localCache[i].isOccupied && localCache[i].blockID==block)// checks if the block is not occupied and chesks if the block is present in the local cache of the processor
                return true;
        }
        
        return false;
    }
    public int getBlockIndex(int block){//get index of the block
        for(int i=0;i<localCache.length;i++){
            if(localCache[i].isOccupied && localCache[i].blockID==block)
                return i;
        }
        return -1;
    }
}
