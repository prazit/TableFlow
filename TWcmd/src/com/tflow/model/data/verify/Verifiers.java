package com.tflow.model.data.verify;

import com.tflow.model.data.TWData;

import java.lang.reflect.Constructor;

public class Verifiers {

    private enum DataVerifiers {
        DatabaseData(DatabaseVerifier.class),
        SFTPData(SFTPVerifier.class),
        ;

        public Class aClass;
        DataVerifiers(Class aClass) {
            this.aClass = aClass;
        }
    }

    public static DataVerifier getVerifier(TWData data) {
        DataVerifiers dataVerifier = DataVerifiers.valueOf(data.getClass().getSimpleName());
        Constructor<DataVerifier> constructor = null;
        DataVerifier dataVerfier = null;

        try {
            constructor = dataVerifier.aClass.getConstructor(TWData.class);
            dataVerfier = constructor.newInstance(data);
            return dataVerfier;

        } catch (Exception ex) {
            /*impossible: NoSuchMethodException: no valid constructor for DataVerifier*/
            /*impossible: InstantiationException: class cannot be instantiated*/
            /*impossible: IllegalAccessException: private/protected constructor*/
            /*possible: InvocationTargetException: error within constructor*/
            return new DefaultVerifier(ex);
        }
    }
}
