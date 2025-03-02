package org.example.dto.response.binary.tree;

import lombok.Data;
import org.example.dto.response.binary.node.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class Tree {
    private Node head;
    private String mainAttribute;

    public static boolean isBiggerAlphabetically(String word1, String word2){
        String alphabetWithNumbers = "ABCDEFGHIJKLMNOPQRSTUVWXYZ" + "0123456789";
        for (int i = 0; i < Math.min(word1.length(), word2.length()); i ++) {
            int charIndex1 = alphabetWithNumbers.indexOf(word1.toLowerCase().charAt(i));
            int charIndex2 = alphabetWithNumbers.indexOf(word2.toLowerCase().charAt(i));
            if (charIndex1 > charIndex2)
                return true;
            else if (charIndex2 > charIndex1)
                return false;
        }
        return false;
    }

    public void insert(Node node){
        if (this.head == null)
            this.head = node;
        else {
            Node currentHead = this.head;
            while (true)
                if (isBiggerAlphabetically(currentHead.getValue().getAttribute(this.mainAttribute), node.getValue().getAttribute(this.mainAttribute)))
                    if (currentHead.getLeft() != null)
                        currentHead = currentHead.getLeft();
                    else {
                        currentHead.setLeft(node);
                        break;
                    }
                else  // if bigger or equal
                    if (currentHead.getRight() != null)
                        currentHead = currentHead.getRight();
                    else {
                        currentHead.setRight(node);
                        break;
                    }
        }
    }



    public void performToAll(String action, Object...args){
        this.performToAll(action, this.head, args);
    }

    private void performToAll(String action, Node currentHead, Object...args){
        if (currentHead != null) {
            currentHead.getValue().performAction(action, args);
            this.performToAll(action, currentHead.getLeft(), args);
            this.performToAll(action, currentHead.getRight(), args);
        }

    }
}



















