package com.tflow.model.editor;

import java.util.ArrayList;

public class StepList<T> extends ArrayList<T> {

    private T negativeItem;

    public void addNegativeItem(T negativeItem) {
        this.negativeItem = negativeItem;
    }

    /**
     * Returns the element at the specified position in this list.
     *
     * @param index of the element to return (valid from -1 to maxOfInt)
     * @Returns the element at the specified position in this list
     * @Throws IndexOutOfBoundsException – if the index is out of range (index >= size())
     */
    @Override
    public T get(int index) {
        if (index < 0) {
            if(negativeItem==null) throw new IndexOutOfBoundsException("need to call addNegativeItem(..) before get negative item");
            return negativeItem;
        }
        return super.get(index);
    }

}
