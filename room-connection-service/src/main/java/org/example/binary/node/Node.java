package org.example.binary.node;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.model.Value.Number;
import org.example.model.Value.Value;

import java.util.List;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Node {
    private Node left;
    private Node right;
    private Value value;

    public String getAttribute(String key){
        return value.getAttribute(key);
    }

    public Node(int val){
        this.value = new Number(val);
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
                return this.right.equals(node) || this.left.equals(node);
            else
                return this.left.equals(node);
        else if (this.right != null)
            return this.right.equals(node);
        return false;

    }
}
