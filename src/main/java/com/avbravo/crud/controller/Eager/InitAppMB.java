package com.avbravo.crud.controller.Eager;



import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import java.io.Serializable;

@Singleton
@Startup
public class InitAppMB implements Serializable {

   
//@Inject
//InitBootsfacesBean initBootsfacesBean;
    @PostConstruct
    public void init() {
//        IntStream.rangeClosed(1, 50)
//                .forEach(i -> create(i));
System.out.println("-----> init()");
//initBootsfacesBean.initializeBootsfacesRenderers(); 
    }


  
}
