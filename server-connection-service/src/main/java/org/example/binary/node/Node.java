package org.example.binary.node;


import lombok.Data;
import org.example.model.Value.Value;

import java.util.List;
import java.util.Map;

@Data
public class Node {
    private Node left;
    private Node right;
    private Value value;

    public String getAttribute(String key){
        return value.getAttribute(key);
    }

    public void copy(Node node){
        this.value = node.value;
        this.right = node.right;
        this.left = node.left;
    }

    public boolean areAttributesValid(Map<String, String> attributesValues){
        for (String key : attributesValues.keySet())
            if (!this.getAttribute(key).equals(attributesValues.get(key)))
                return false;
        return true;
    }

    public boolean isParentOf(Node node){
        if (this.left != null)
            if (this.right != null)
                return this.right == node || this.left == node;
            else
                return this.left== node;
        else if (this.right != null)
            return this.right == node;
        return false;

    }
}
