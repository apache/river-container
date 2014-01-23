/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.apache.river.container.admin.api;

import java.io.Serializable;

/**
 *
 * @author trasukg
 */
public class ApplicationInfo implements Serializable {
    String name;
    ApplicationStatus status;

    public ApplicationInfo(String name, ApplicationStatus status) {
        this.name = name;
        this.status = status;
    }
    
}
