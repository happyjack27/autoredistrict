package util;

import geography.Vertex;

import java.util.*;
 
public class ClosestPair
{
 
  public static class Pair
  {
    public Vertex point1 = null;
    public Vertex point2 = null;
    public double distance = 0.0;
 
    public Pair()
    {  }
 
    public Pair(Vertex point1, Vertex point2)
    {
      this.point1 = point1;
      this.point2 = point2;
      calcDistance();
    }
 
    public void update(Vertex point1, Vertex point2, double distance)
    {
      this.point1 = point1;
      this.point2 = point2;
      this.distance = distance;
    }
 
    public void calcDistance()
    {  this.distance = distance(point1, point2);  }
 
    public String toString()
    {  return point1 + "-" + point2 + " : " + distance;  }
  }
 
  public static double distance(Vertex p1, Vertex p2)
  {
    double xdist = p2.x - p1.x;
    double ydist = p2.y - p1.y;
    return Math.hypot(xdist, ydist);
  }
 
  public static Pair bruteForce(List<? extends Vertex> points)
  {
    int numPoints = points.size();
    if (numPoints < 2)
      return null;
    Pair pair = new Pair(points.get(0), points.get(1));
    if (numPoints > 2)
    {
      for (int i = 0; i < numPoints - 1; i++)
      {
        Vertex point1 = points.get(i);
        for (int j = i + 1; j < numPoints; j++)
        {
          Vertex point2 = points.get(j);
          double distance = distance(point1, point2);
          if (distance < pair.distance)
            pair.update(point1, point2, distance);
        }
      }
    }
    return pair;
  }
 
  public static void sortByX(List<? extends Vertex> points)
  {
    Collections.sort(points, new Comparator<Vertex>() {
        public int compare(Vertex point1, Vertex point2)
        {
          if (point1.x < point2.x)
            return -1;
          if (point1.x > point2.x)
            return 1;
          return 0;
        }
      }
    );
  }
 
  public static void sortByY(List<? extends Vertex> points)
  {
    Collections.sort(points, new Comparator<Vertex>() {
        public int compare(Vertex point1, Vertex point2)
        {
          if (point1.y < point2.y)
            return -1;
          if (point1.y > point2.y)
            return 1;
          return 0;
        }
      }
    );
  }
 
  public static Pair divideAndConquer(List<? extends Vertex> points)
  {
    List<Vertex> pointsSortedByX = new ArrayList<Vertex>(points);
    sortByX(pointsSortedByX);
    List<Vertex> pointsSortedByY = new ArrayList<Vertex>(points);
    sortByY(pointsSortedByY);
    return divideAndConquer(pointsSortedByX, pointsSortedByY);
  }
 
  private static Pair divideAndConquer(List<? extends Vertex> pointsSortedByX, List<? extends Vertex> pointsSortedByY)
  {
    int numPoints = pointsSortedByX.size();
    if (numPoints <= 3)
      return bruteForce(pointsSortedByX);
 
    int dividingIndex = numPoints >>> 1;
    List<? extends Vertex> leftOfCenter = pointsSortedByX.subList(0, dividingIndex);
    List<? extends Vertex> rightOfCenter = pointsSortedByX.subList(dividingIndex, numPoints);
 
    List<Vertex> tempList = new ArrayList<Vertex>(leftOfCenter);
    sortByY(tempList);
    Pair closestPair = divideAndConquer(leftOfCenter, tempList);
 
    tempList.clear();
    tempList.addAll(rightOfCenter);
    sortByY(tempList);
    Pair closestPairRight = divideAndConquer(rightOfCenter, tempList);
 
    if (closestPairRight.distance < closestPair.distance)
      closestPair = closestPairRight;
 
    tempList.clear();
    double shortestDistance =closestPair.distance;
    double centerX = rightOfCenter.get(0).x;
    for (Vertex point : pointsSortedByY)
      if (Math.abs(centerX - point.x) < shortestDistance)
        tempList.add(point);
 
    for (int i = 0; i < tempList.size() - 1; i++)
    {
      Vertex point1 = tempList.get(i);
      for (int j = i + 1; j < tempList.size(); j++)
      {
        Vertex point2 = tempList.get(j);
        if ((point2.y - point1.y) >= shortestDistance)
          break;
        double distance = distance(point1, point2);
        if (distance < closestPair.distance)
        {
          closestPair.update(point1, point2, distance);
          shortestDistance = distance;
        }
      }
    }
    return closestPair;
  }
 
  public static void main(String[] args)
  {
    int numPoints = (args.length == 0) ? 1000 : Integer.parseInt(args[0]);
    List<Vertex> points = new ArrayList<Vertex>();
    Random r = new Random();
    for (int i = 0; i < numPoints; i++)
      points.add(new Vertex(r.nextDouble(), r.nextDouble()));
    System.out.println("Generated " + numPoints + " random points");
    long startTime = System.currentTimeMillis();
    Pair bruteForceClosestPair = bruteForce(points);
    long elapsedTime = System.currentTimeMillis() - startTime;
    System.out.println("Brute force (" + elapsedTime + " ms): " + bruteForceClosestPair);
    startTime = System.currentTimeMillis();
    Pair dqClosestPair = divideAndConquer(points);
    elapsedTime = System.currentTimeMillis() - startTime;
    System.out.println("Divide and conquer (" + elapsedTime + " ms): " + dqClosestPair);
    if (bruteForceClosestPair.distance != dqClosestPair.distance)
      System.out.println("MISMATCH");
  }
}