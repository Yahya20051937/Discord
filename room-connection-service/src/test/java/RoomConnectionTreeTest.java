import org.example.binary.node.Node;
import org.example.binary.node.RoomConnectionNode;
import org.example.binary.tree.RoomConnectionsTree;
import org.example.binary.tree.Tree;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class RoomConnectionTreeTest {
    @InjectMocks
    RoomConnectionsTree roomConnectionsTree;

    @Test
    void testInsert(){
        int i = 0;
        while (i <= 1000) {
            i += 1;
            roomConnectionsTree.insert(new RoomConnectionNode());
        }
        assertNode((RoomConnectionNode) roomConnectionsTree.getHead());
    }

    @Test
    void testFindByRoomId(){
        String roomId = UUID.randomUUID().toString();
        RoomConnectionNode node1  = new RoomConnectionNode(roomId);
        RoomConnectionNode node2  = new RoomConnectionNode(roomId);
        RoomConnectionNode node3  = new RoomConnectionNode(roomId);
        int random1 = new Random().nextInt(1, 100);
        int random2 = new Random().nextInt(1, 100);
        int random3 = new Random().nextInt(1, 100);
        for (int i = 1; i < 100 ; i ++)
            if (i == random1)
                roomConnectionsTree.insert(node1);
            else if (i == random2)
                roomConnectionsTree.insert(node2);
            else if (i == random3)
                roomConnectionsTree.insert(node3);
            else
                roomConnectionsTree.insert(new RoomConnectionNode(UUID.randomUUID().toString()));

        List<RoomConnectionNode> result = roomConnectionsTree.findAllByRoomId(roomId);
        this.assertListsEqual(List.of(node1, node2, node3), result);


    }

    @Test
    void testFindByIdAndRoomId(){
        String roomId = UUID.randomUUID().toString();
        RoomConnectionNode node1  = new RoomConnectionNode(roomId, "yahya");
        int random1 = new Random().nextInt(1, 100);
        for (int i = 1; i < 100 ; i ++)
            if (i == random1)
                roomConnectionsTree.insert(node1);
            else
                roomConnectionsTree.insert(new RoomConnectionNode(UUID.randomUUID().toString(), UUID.randomUUID().toString()));
        RoomConnectionNode result = roomConnectionsTree.findByRoomIdAndUsername(roomId, "yahya");
        assertEquals(result, node1);
    }

    @Test
    void testFindParent(){
        for (int i = 1; i < 100 ; i ++)
            roomConnectionsTree.insert(new RoomConnectionNode(UUID.randomUUID().toString(), UUID.randomUUID().toString()));

        this.assertParent((RoomConnectionNode) roomConnectionsTree.getHead());


    }

    @Test
    void testDelete(){
        Tree tree = new Tree();
        List<Integer> values = new ArrayList<>();
        for (int i = 1; i < 100 ; i ++){
            int random1 = new Random().nextInt(0, 9);
            if (!values.contains(random1))
                values.add(random1);
        }

        for (Integer value : values)
            tree.insert(new Node(value));

        this.assertNode(tree.getHead(), "value");

        Integer random1;
        Integer random2;
        do {
            random1 = new Random().nextInt(0, 9);
            random2 = new Random().nextInt(0, 9);
        } while (random1.equals(Integer.valueOf(tree.getHead().getValue().getAttribute(""))) && random2.equals(Integer.valueOf(tree.getHead().getValue().getAttribute(""))) && !random1.equals(random2));

        tree.delete(String.valueOf(random1), Map.of());
        tree.delete(String.valueOf(random2), Map.of());
        tree.delete(tree.getHead().getValue().getAttribute(""), Map.of());
        this.assertNode(tree.getHead(), "value");
    }

    @Test
    void test2(){
        RoomConnectionsTree tree = new RoomConnectionsTree();
        Node node1 = new RoomConnectionNode("19c7c95d-be4a-475a-be7f-c4d09a88ace0", "yahya");
        Node node2 = new RoomConnectionNode("19c7c95d-be4a-475a-be7f-c4d09a88ace0", "anorak");
        Node node3 = new RoomConnectionNode("29383329-4f32-43c8-afe3-ea5e6a610cdb", "anorak");
        tree.insert(node1);
        tree.insert(node2);
        tree.insert(node3);
        //assertEquals(tree.findParent(node2), node1);
        tree.deleteAndPerform("19c7c95d-be4a-475a-be7f-c4d09a88ace0", Map.of("username", "anorak"), tree.getHead(), "action");
        //tree.deleteNode(tree.getHead().getLeft());

    }

    @Test
    void testDeleteRoomConnection(){
        List<String> roomIds = this.generateRoomIds();
        List<String> roomsIdsAndUsernames = new ArrayList<>();
        for (int i = 0; i < 1000 ; i ++) {
            int random2 = new Random().nextInt(0, roomIds.size());
            String roomId = roomIds.get(random2);
            String username = UUID.randomUUID().toString();
            roomsIdsAndUsernames.add(roomId + "//" + username);
            roomConnectionsTree.insert(new RoomConnectionNode(roomId, username));
        }

        while (!roomsIdsAndUsernames.isEmpty()){
            int random1 = new Random().nextInt(0, roomsIdsAndUsernames.size());
            String roomId = roomsIdsAndUsernames.get(random1).split("//")[0];
            String username = roomsIdsAndUsernames.get(random1).split("//")[1];
            roomConnectionsTree.deleteAndPerform(roomId, Map.of("username", username),roomConnectionsTree.getHead(), "");
            assertNull(roomConnectionsTree.findByRoomIdAndUsername(roomId, username));   // the value is deleted.
            roomsIdsAndUsernames.remove(random1);
            this.assertNode((RoomConnectionNode) roomConnectionsTree.getHead());   // the tree is well-structured.
            for (String identifier : roomsIdsAndUsernames){
                String roomId1 = identifier.split("//")[0];
                String username1 = identifier.split("//")[1];
                assertNotNull(roomConnectionsTree.findByRoomIdAndUsername(roomId1, username1)); // all other values are present
            }


        }



    }

    List<String> generateRoomIds(){
        List<String> roomIds = new ArrayList<>();
        int j = 0;
        while (j < 100) {
            j += 1;
            roomIds.add(UUID.randomUUID().toString());
        }
        return roomIds;
    }


    void assertNode(RoomConnectionNode node){
        if (node == null)
            return;
        if (node.getLeft() != null) {
            assertTrue(RoomConnectionsTree.isBiggerAlphabetically(node.getNodeValue().getRoomId(), node.getLeftNode().getNodeValue().getRoomId()));
            assertNode(node.getLeftNode());
        }
        if (node.getRight() != null) {  // right node should be bigger or equal
            assertFalse(RoomConnectionsTree.isBiggerAlphabetically(node.getNodeValue().getRoomId(), node.getRightNode().getNodeValue().getRoomId()));
            assertNode(node.getRightNode());
        }

    }

    void assertNode(Node node, String mainAttribute){
        if (node == null)
            return;
        if (node.getLeft() != null) {
            assertTrue(RoomConnectionsTree.isBiggerAlphabetically(node.getValue().getAttribute(mainAttribute), node.getLeft().getValue().getAttribute(mainAttribute)));
            assertNode(node.getLeft(),  mainAttribute);
        }
        if (node.getRight() != null) {
            assertTrue(RoomConnectionsTree.isBiggerAlphabetically(node.getRight().getValue().getAttribute(mainAttribute), node.getValue().getAttribute(mainAttribute)));
            assertNode(node.getRight(),  mainAttribute);
        }
    }

    void assertListsEqual(List<RoomConnectionNode> expected, List<RoomConnectionNode> actual){
        assertEquals(expected.size(), actual.size());
        int foundElements = 0;
        for (int i = 0; i < expected.size(); i++)
            for (int j = 0; j < actual.size(); j++)
                if (expected.get(i) == actual.get(j))
                    foundElements  += 1;
        assertEquals(expected.size(), foundElements);
    }

    void assertParent(RoomConnectionNode node){
        if (node.getLeft() != null) {
            assertEquals(node, roomConnectionsTree.findParent(node.getLeftNode()));
            this.assertParent(node.getLeftNode());
        }

        if (node.getRight() != null) {
            assertEquals(node, roomConnectionsTree.findParent(node.getRightNode()));
            this.assertParent(node.getRightNode());
        }

    }


}
