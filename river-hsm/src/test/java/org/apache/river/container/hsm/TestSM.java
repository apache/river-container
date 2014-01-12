/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.river.container.hsm;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author trasukg
 */
@RootState({TestSMInterface.class, TestSMSecondInterface.class})
public class TestSM {

    @State({A.class, B.class, C.class})
    @Initial(A.class)
    @Retained
    public Object state;
    
    /* Really shouldn't need to be public - we'll fix shortly. */
    @Controller
    public StateMachineInfo controller;
    int nullTransitionEntryCount = 0;
    int aEntryCount = 0, aExitCount = 0;

    public Object returnNull() {
        return null;
    }
    
    public List<Class> getActiveStates() {
        try {
            return controller.getActiveStates();
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(TestSM.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        } 
    }

    @Transition(A.class)
    public void gotoA() {
        //controller.transition(A.class);
    }

    @Transition(B.class)
    public void gotoB() {
        //controller.transition(B.class);
    }

    @Transition(C.class)
    public void doSecondInterfaceAction() { }
    
    public int getAEntryCount() {
        return aEntryCount;
    }

    public int getAExitCount() {
        return aExitCount;
    }

    public int getNullTransitionEntryCount() {
        return nullTransitionEntryCount;
    }

    public String sayConstantHello() {
        return "Hello";
    }

    @Guard(A.class)
    public boolean beFalse() {
        return false;
    }
    
    public class A {

        @State({A1.class})
        @Initial(A1.class)
        public Object state;
        
        @Transition(B.class)
        public String sayHello() {
            //controller.transition(B.class);
            return "Hello";
        }

        @Transition(A.class)
        public void nullTransition() {
            //controller.transition(A.class);
        }

        @OnEntry
        public void onEntry() {
            aEntryCount++;
            nullTransitionEntryCount++;
        }

        @OnExit
        public void onExit() {
            aExitCount++;
        }
        
        public class A1 {}
    }

    public class B {

        @State({B1.class, B2.class, B3.class})
        @Initial(B1.class)
        Object state;

        public String sayHello() {
            return "There";
        }
    }

    public class B1 {

        @Transition(B2.class)
        public void moveSubstateOfB() {
            
        }
    }

    public class B2 {
    }

    public class B3 {
    }
    
    public class C {
        public String sayHello() {
            return "HelloFromC";
        }
    }
}
