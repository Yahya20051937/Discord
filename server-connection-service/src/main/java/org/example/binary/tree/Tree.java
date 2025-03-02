package org.example.binary.tree;

import lombok.Data;
import org.example.binary.node.Node;

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

    public Node findByAttributes(String mainValue, Map<String, String> attributesValues){
        return this.findByAttributes(mainValue, attributesValues, this.head);
    }

    private Node findByAttributes(String mainValue, Map<String, String> attributesValues, Node currentHead){
        if (currentHead == null)
            return null; // if we reach this, the value is not present

        if (isBiggerAlphabetically(currentHead.getValue().getAttribute(this.mainAttribute), mainValue))
            return this.findByAttributes(mainValue, attributesValues, currentHead.getLeft());

        else {
            if (currentHead.getValue().getAttribute(this.mainAttribute).equals(mainValue) && currentHead.areAttributesValid(attributesValues)) // the first one that we find is the result
                return currentHead;
            return this.findByAttributes(mainValue, attributesValues, currentHead.getRight()); // bigger or (equal and attributes are  different)
        }
    }

    public List<Node> findAll(String mainValue){
        List<Node> result = new ArrayList<>();
        this.findAll(mainValue, this.head, result);
        return result;
    }

    private void findAll(String mainValue, Node currentHead, List<Node> result){
        if (currentHead == null)
            return;

        if (isBiggerAlphabetically(mainValue, currentHead.getValue().getAttribute(this.mainAttribute)))
            this.findAll(mainValue, currentHead.getRight(), result);

        else if (isBiggerAlphabetically(currentHead.getValue().getAttribute(this.mainAttribute), mainValue))
            this.findAll(mainValue, currentHead.getLeft(), result);

        else {
            this.findAll(mainValue, currentHead.getRight(), result); // roomId == currentHead.getValue.getRoomId
            this.findAll(mainValue, currentHead.getLeft(), result);
            result.add(currentHead);
        }

    }

    public void findAllAndPerform(String mainValue, Node currentHead, String action, Object...args){
        if (currentHead == null)
            return;
        if (isBiggerAlphabetically(mainValue, currentHead.getValue().getAttribute(this.mainAttribute)))
            this.findAllAndPerform(mainValue, currentHead.getRight(), action, args);

        else if (isBiggerAlphabetically(currentHead.getAttribute(this.mainAttribute), mainValue))
            this.findAllAndPerform(mainValue, currentHead.getLeft(), action, args);

        else {
            this.findAllAndPerform(mainValue, currentHead.getRight(), action, args); // roomId == currentHead.getValue.getRoomId
            this.findAllAndPerform(mainValue, currentHead.getLeft(), action, args);
                currentHead.getValue().performAction(action, args);
        }
    }

    public void deleteAndPerform(String mainValue,Map<String, String> attributes, Node currentHead, String action, Object...args){
        if (currentHead == null)
            return;

        if (isBiggerAlphabetically(mainValue, currentHead.getValue().getAttribute(this.mainAttribute)))
            this.deleteAndPerform(mainValue,attributes, currentHead.getRight(), action, args);

        else if (isBiggerAlphabetically(currentHead.getAttribute(this.mainAttribute), mainValue))
            this.deleteAndPerform(mainValue,attributes, currentHead.getLeft(), action, args);

        else {

        this.deleteAndPerform(mainValue,attributes, currentHead.getRight(), action, args); // roomId == currentHead.getValue.getRoomId
        this.deleteAndPerform(mainValue,attributes, currentHead.getLeft(), action, args);
        if (currentHead.areAttributesValid(attributes)) {
            currentHead.getValue().performAction(action, args);
            this.deleteNode(currentHead);
        }
    }
    }



    public Node findParent(Node node){
        return this.findParent(node, this.head);
    }

    private Node findParent(Node node, Node currentHead){
        if (currentHead == null || currentHead.equals(node))
            return null;

        if (currentHead.isParentOf(node))
            return currentHead;

        if (isBiggerAlphabetically(currentHead.getValue().getAttribute(this.mainAttribute), node.getValue().getAttribute(this.mainAttribute)))
            return this.findParent(node, currentHead.getLeft());
        else
            return this.findParent(node, currentHead.getRight());
    }

    public void delete(String mainValue, Map<String, String> attributesValue){
        Node node = this.findByAttributes(mainValue, attributesValue);
        this.deleteNode(node);
    }

    public void deleteNode(Node node){
        /*
        * If the right node is not null, we copy it to the node we want to delete, but now we have to insert the left node if not null,
        *  we are sure that the node's left is smaller than anything on the right, so we keep looping until we find a free left node on the left,
        * and we put the node's left in it., if the right is null and left is not, we just copy the left node ,  else we find the parent and make the node null.
        *
        * */
        if (node != null)
            if (node.getRight()  != null){
                Node leftNode = node.getLeft();
                Node rightNode = node.getRight();
                node.copy(rightNode);
                if (leftNode != null){   // inserting the left node.
                    Node currentNode = node;
                    while (true)
                        if (currentNode.getLeft() == null){
                            currentNode.setLeft(leftNode);
                            break;
                        }
                        else
                            currentNode = currentNode.getLeft();
                }

            }
            else if (node.getLeft() != null){
                Node leftNode = node.getLeft();
                node.copy(leftNode);
            }
            else {
                Node parent = this.findParent(node);
                if (parent == null)
                    this.head = null;
                else if (isBiggerAlphabetically(parent.getValue().getAttribute(this.mainAttribute), node.getValue().getAttribute(this.mainAttribute)))
                    parent.setLeft(null);
                else
                    parent.setRight(null); // if bigger or equal
            }


    }



    public void performToAll(String action, Object...args){
        this.performToAll(this.head, action, args);
    }

    private void performToAll(Node currentHead, String action, Object...args){
        currentHead.getValue().performAction(action, args);
        if (currentHead.getRight() != null)
            this.performToAll(currentHead.getRight(), action, args);
        if (currentHead.getLeft() != null)
            this.performToAll(currentHead.getLeft(), action, args);
    }




}



















