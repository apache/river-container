/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.river.container.hsm;

/**
 * Holds a transition on a substate.
 */
public class TransitionOnSubstate {
    
    // package access, so the executor can use it directly.
    SubstateInfo substate;

    /**
     * Create an instance specifying the target metastate for the given
     * substate.
     * 
     * @param substate
     * @param targetMetaState 
     */
    public TransitionOnSubstate(SubstateInfo substate, MetaState targetMetaState) {
        this.substate = substate;
        this.targetMetaState = targetMetaState;
    }

    public SubstateInfo getSubstate() {
        return substate;
    }


    public MetaState getTargetMetaState() {
        return targetMetaState;
    }
    
    // package access, so the executor can use it directly.
    MetaState targetMetaState;
    
    
}
