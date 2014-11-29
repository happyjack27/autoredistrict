package mapCandidates;
public class Edge {
    Block block1;
    Block block2;
    Vertex vertex1;
    Vertex vertex2;
    double length;
    boolean areBothSidesSameDistrict(int[] block_districts) {
        return block_districts[block1.index] == block_districts[block2.index];
    }
}