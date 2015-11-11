package com.ganesus.facevision.engine;

import java.util.ArrayList;
import java.util.List;


public class KMeans {
    public static class Cluster {
        public Point centroid;
        public List<Point> pointList;
    }

    public List<Cluster> computeCluster(List<Point> centroid, List<Point> data, int nIteration,int clusterDistance) {

        List<Point> nextCentroid = new ArrayList<>();
        for (int i=0;i<centroid.size();i++) {
            nextCentroid.add(new Point(0,0));
        }

        List<Point> currentCentroid = new ArrayList<Point>(centroid);
        while (nIteration!=0) {
            for (int i = 0; i < centroid.size(); i++) {
                nextCentroid.get(i).x = 0;
                nextCentroid.get(i).y = 0;
            }

            int count[] = new int[centroid.size()];

            for (int i = 0; i < centroid.size(); i++) {
                count[i] = 0;
            }
            for (int i = 0; i < data.size(); i++) {
                int nearestCentroidIndex = 0;
                int minDistance = -1;

                for (int j = 0; j < centroid.size(); j++) {
                    int diffX = data.get(i).x - currentCentroid.get(j).x;
                    int diffY = data.get(i).y - currentCentroid.get(j).y;

                    int distance = diffX * diffX + diffY * diffY;
                    if (minDistance > distance || minDistance == -1) {
                        minDistance = distance;
                        nearestCentroidIndex = j;
                    }
                }

                if (minDistance < clusterDistance) {
                    count[nearestCentroidIndex]++;
                    nextCentroid.get(nearestCentroidIndex).x += data.get(i).x;
                    nextCentroid.get(nearestCentroidIndex).y += data.get(i).y;
                }

            }

            for (int i = 0; i < centroid.size(); i++) {
                if (count[i]!=0) {
                    nextCentroid.get(i).x /= count[i];
                    nextCentroid.get(i).y /= count[i];

                    currentCentroid.get(i).x = nextCentroid.get(i).x;
                    currentCentroid.get(i).y = nextCentroid.get(i).y;
                }
            }

            nIteration --;
        }

        List<Cluster> clusters = new ArrayList<>();
        for (int i=0;i<centroid.size();i++) {
            clusters.add(new Cluster());
            clusters.get(i).centroid = new Point(0,0);
            clusters.get(i).centroid.setPoint(currentCentroid.get(i));
            clusters.get(i).pointList = new ArrayList<>();
        }

        for (int i = 0; i < data.size(); i++) {
            int nearestCentroidIndex = 0;
            int minDistance = -1;

            for (int j = 0; j < centroid.size(); j++) {
                int diffX = data.get(i).x - currentCentroid.get(j).x;
                int diffY = data.get(i).y - currentCentroid.get(j).y;

                int distance = diffX * diffX + diffY * diffY;
                if (minDistance > distance || minDistance == -1) {
                    minDistance = distance;
                    nearestCentroidIndex = j;
                }
            }

            if (minDistance < clusterDistance) {
                clusters.get(nearestCentroidIndex).pointList.add(data.get(i));
            }

        }
        return clusters;
    }
}
