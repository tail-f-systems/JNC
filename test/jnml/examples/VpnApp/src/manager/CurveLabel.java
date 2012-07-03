package manager;

import java.awt.*;
import java.awt.event.*;
import java.awt.font.*;
import java.awt.geom.*;
import javax.swing.*;

public class CurveLabel {
    GeneralPath path;
    Point2D.Double[] points;
    double[] tokenWidths;
    double offset;
    Point2D.Double previousPoint = null;
    
    CurveLabel(Graphics2D g2, QuadCurve2D.Double curve, String label,
	       Color color, Font font, FontRenderContext frc) {
	FontMetrics fm = g2.getFontMetrics(font);
	long baseline = Math.round(fm.getHeight()/2.0/2.0); // heuristics!
	tokenWidths = getTokenWidths(label, font, frc);
     	assignPath(curve);

	g2.setColor(color);
        g2.draw(path);
	g2.setColor(Color.black);
	
        for(int j = 0; j < points.length; j++)
	    g2.clearRect((int)Math.round(points[j].x-6),
			 (int)Math.round(points[j].y-6), 12, 12);
	
        String[] tokens = label.split("(?<=[\\w\\s/\\.-])");
	
        for(int j = 0; j < points.length-1; j++) {
            double theta = getAngle(j);
            AffineTransform at =
                AffineTransform.getTranslateInstance(points[j].x, points[j].y);
            at.rotate(theta);	    
            g2.setFont(font.deriveFont(at));
            g2.drawString(tokens[j], 0, baseline);
        }
    }
 
    private double getAngle(int index) {
        double dy = points[index+1].y - points[index].y;
        double dx = points[index+1].x - points[index].x;
        return Math.atan2(dy, dx);
    }
    
    private void assignPath(Shape s) {
        path = new GeneralPath(s);
        Point2D.Double[] p = getPathPoints();
        collectLayoutPoints(p);
    }
    
    private Point2D.Double[] getPathPoints() {
        double flatness = 0.01;
        PathIterator pit = path.getPathIterator(null, flatness);
        int count = 0;
	
        while(!pit.isDone()) {
            count++;
            pit.next();
        }
	
	//System.out.println("Points: +"+count);
        Point2D.Double[] p = new Point2D.Double[count];
        pit = path.getPathIterator(null, flatness);
        double[] coords = new double[6];
        count = 0;
	double length = 0;
	
        while(!pit.isDone()) {
            int type = pit.currentSegment(coords);
	    
            switch(type) {
                case PathIterator.SEG_MOVETO:
                case PathIterator.SEG_LINETO:
                    p[count++] = new Point2D.Double(coords[0], coords[1]);

		    if (count > 1)
			length += p[count-2].distance(p[count-1]);
		    
                    break;
                case PathIterator.SEG_CLOSE:
                    break;
                default:
                    System.out.println("unexpected type: " + type);
            }

            pit.next();
        }

	// Calculate offset
	this.offset = length/2.0;
        
	for(int j = 0; j < tokenWidths.length/2; j++)
            this.offset -= tokenWidths[j];

	this.offset -= 25; // heuristics!

        return p;
    }
 
    private double[] getTokenWidths(String label, Font font,
				    FontRenderContext frc) {
        String[] tokens = label.split("(?<=[\\w\\s/\\.-])");
        double[] widths = new double[tokens.length];

        for(int j = 0; j < tokens.length; j++) {
            Rectangle2D rect = font.getStringBounds(tokens[j], frc);
            float width = (float)(rect.getWidth());
            widths[j] = width;
        }

        return widths;
    }
 
    private void collectLayoutPoints(Point2D.Double[] p) {
        int index = 0;
        int n = tokenWidths.length;
        double distance = offset;
        points = new Point2D.Double[n+1];
        
        for(int j = 0; j < tokenWidths.length; j++) {
            index = getNextPointIndex(p, index, distance);
            points[j] = p[index];
            distance = tokenWidths[j];
        }

        index = getNextPointIndex(p, index, tokenWidths[n-1]);
        points[points.length-1] = p[index];
    }
 
    private int getNextPointIndex(Point2D.Double[] p,
                                  int start, double targetDist) {
        for(int j = start; j < p.length; j++) {
            double distance = p[j].distance(p[start]);

            if(distance > targetDist)
		return j;
        }
        
        return start;
    }
}
