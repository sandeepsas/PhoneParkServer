package ParkRouter;

public class ParkEdge {
	private ParkNode node1, node2;
	private int nBlocks;
	private int blockId1, blockId2;
	private int total1, total2;
	private boolean oneWay;

	public ParkEdge(ParkNode n1, ParkNode n2, int nB, int bId1, int bId2, int tot1, int tot2, boolean oneWay) {
		this.oneWay = oneWay;
		node1 = n1;
		node2 = n2;

		if (nB == 0) {
			nBlocks = nB;
			if ((bId1 == -1) && (bId2 == -1)) {
				blockId1 = bId1;
				blockId2 = bId2;
				total1 = tot1;
				total2 = tot2;
			} else {
				System.out.println("SFParkEdge should have had blockId's of -1 because it has 0 SFPark blocks.");
				System.exit(1);
			}
		} else if (nB == 1) {
			nBlocks = nB;
			if ((bId1 != -1) && (bId2 == -1)) {
				blockId1 = bId1;
				blockId2 = bId2;
				total1 = tot1;
				total2 = tot2;
			} else {
				System.out.println("SFParkEdge should have had blockId2 of -1 because it has only 1 SFPark block.");
				System.exit(1);
			}
		} else if (nB == 2) {
			nBlocks = nB;
			if ((bId1 != -1) && (bId2 != -1)) {
				blockId1 = bId1;
				blockId2 = bId2;
				total1 = tot1;
				total2 = tot2;
			} else {
				System.out.println(
						"SFParkEdge should have had blockId's not equal to -1.  They both should be a valid id.");
				System.exit(1);
			}
		} else {
			// nB should have been 0, 1, or 2. Raise an ERROR!!
			System.out.println("nBlocks can only have a value of 0, 1, or 2.");
			System.exit(1);
		}
	}

	public ParkNode getNode1() {
		// System.out.println("Hey guys 1!!");
		return node1;
	}

	public ParkNode getNode2() {
		// System.out.println("Hey guys 2!!");
		return node2;
	}

	public boolean isOneWay() {
		return this.oneWay;
	}

	public int getNBlocks() {
		return nBlocks;
	}

	public int getBlockId1() {
		return blockId1;
	}

	public int getBlockId2() {
		return blockId2;
	}

	public int getTotal1() {
		return total1;
	}

	public int getTotal2() {
		return total2;
	}
}
