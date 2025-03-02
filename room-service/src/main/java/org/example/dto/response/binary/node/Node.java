package org.example.dto.response.binary.node;


import lombok.Data;
import org.example.dto.response.Value;

import java.util.List;
import java.util.Map;

@Data
public class Node {
    private Node left;
    private Node right;
    private Value value;
}
