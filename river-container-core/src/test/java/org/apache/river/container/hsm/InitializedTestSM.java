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

/**
 * State machine to test whether the state machine correctly uses the supplied
 * root state object.
 */
@RootState(InitializedTestSMInterface.class)
public class InitializedTestSM {

    private int value = 0;

    @State({Locked.class, Unlocked.class, Armed.class})
    @Initial(Locked.class)
    Object lockedState;

    public int getValue() {
        return value;
    }

    public class Locked {

        @Transition(Unlocked.class)
        public void unlock() {
            System.out.println("Locked.unlock()");
        }

        public void setValue(int v) {
            throw new IllegalStateException("Locked!");
        }

        @Transition(Armed.class)
        public void arm() {
        }
    }

    public class Armed extends Locked {
    }

    public class Unlocked {

        @Transition(Locked.class)
        public void lock() {
            System.out.println("Unlocked.lock()");
        }

        public void setValue(int v) {
            value = v;
        }
    }
}
