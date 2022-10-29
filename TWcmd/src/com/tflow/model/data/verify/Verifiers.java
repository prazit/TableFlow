package com.tflow.model.data.verify;

import com.tflow.model.data.TWData;

import java.lang.reflect.Constructor;

public class Verifiers {
    public static DataVerifier getVerifier(TWData data) {
        try {
            String className = data.getClass().getSimpleName();
            Class aClass = Class.forName("com.tflow.model.data.verify." + className.substring(0, className.length() - 4) + "Verifier");
            Constructor<DataVerifier> constructor = aClass.getConstructor(TWData.class);
            return constructor.newInstance(data);

        } catch (Exception ex) {
            /*possible: ClassNotFoundException: no verifier class for data*/
            /*impossible: NoSuchMethodException: no valid constructor for DataVerifier*/
            /*impossible: InstantiationException: class cannot be instantiated*/
            /*impossible: IllegalAccessException: private/protected constructor*/
            /*possible: InvocationTargetException: error within constructor*/
            return new DefaultVerifier(ex, data);
        }
    }
}
