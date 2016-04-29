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

public class Processor {
    
    public LocalCacheBlock[] localCache;

    public Processor(int cntBlocks) {
        this.localCache=new LocalCacheBlock[cntBlocks];
        for(int i=0;i<cntBlocks;i++){
            this.localCache[i]=new LocalCacheBlock();
            this.localCache[i].blockID=-1;
            
        }
    }
    public int getALocationToPlaceBlock(){ //temporary simple search need to impelement LRU
        int lruIndex=-1,lruVal=-1;
        for(int i=0;i<this.localCache.length;i++){
            if(!this.localCache[i].isOccupied)
                return i;
            
            if(lruVal<this.localCache[i].lastUsed){
                lruIndex=i;
                lruVal=this.localCache[i].lastUsed;
            }
        }
        
        return lruIndex;
    }
    public boolean containsBlock(int block){
        for(int i=0;i<localCache.length;i++){
            if(localCache[i].isOccupied && localCache[i].blockID==block)
                return true;
        }
        
        return false;
    }
    public int getBlockIndex(int block){
        for(int i=0;i<localCache.length;i++){
            if(localCache[i].isOccupied && localCache[i].blockID==block)
                return i;
        }
        return -1;
    }
}
