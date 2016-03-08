package ParkRouter;
public class ParkLocation
{
    private ParkEdge edge;
    private int direction;
    private double offset;

    public ParkLocation(ParkEdge e, int d, double o)
    {
        edge = e;
        direction = d;
        offset = o;
    }

    public ParkEdge getEdge()
    {
        return edge;
    }

    public int getDirection()
    {
        return direction;
    }

    public double getOffset()
    {
        return offset;
    }

    public void setEdge(ParkEdge e)
    {
        edge = e;
    }

    public void setDirection(int d)
    {
        direction = d;
    }

    public void setOffset(double o)
    {
        offset = o;
    }
}