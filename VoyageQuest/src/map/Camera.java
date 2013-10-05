package map;

import org.lwjgl.opengl.GL11;
import org.newdawn.slick.Image;
import org.newdawn.slick.geom.Rectangle;
import scripting.Parameter;
import scripting.Thread;
import voyagequest.Global;
import voyagequest.DoubleRect;
import voyagequest.Util;
import voyagequest.VoyageQuest;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.ListIterator;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.Color;
import org.newdawn.slick.tiled.Layer;
import org.newdawn.slick.SlickException;

/**
 *
 * @author Edmund
 */

public class Camera {
    //Locations of camera relative to map
    private double x, y;
    private double screenCenterX;
    private double screenCenterY;

    //Pan information
    private double panVx = 0;
    private double panVy = 0;

    //Is the Camera being frozen to a specific location?
    private boolean frozen;

    // The alpha map being applied for cave effect
    private Image caveLight;
    public static boolean lightSourceOn = false;

    //Fade is how visible the screen is. 255 for max, 0 for completely dark.
    public static int fade = 255;
    
    public Camera() 
    {

        //Load the lighting... This will be changed later, of course.
        InputStream is = getClass().getClassLoader().getResourceAsStream("res/alphamini.png");

        try {
            caveLight = new Image(is, "res/alphamini.png", false, Image.FILTER_NEAREST);
        } catch (Exception e) {
            System.out.println("Failed to load alpha lighting - check Camera class");
        }

        //Calculate some useful things...
        screenCenterX = VoyageQuest.X_RESOLUTION/2.0;
        screenCenterY = VoyageQuest.Y_RESOLUTION/2.0;
        
        frozen = false;
        x = 0.0d;
        y = 0.0d;
    }

    public void setPanVelocity(double panVx, double panVy)
    {
        this.panVx = panVx;
        this.panVy = panVy;
    }

    public boolean continuePan(double delta, Thread actingThread)
    {
        //Get the distance left to move...
        Parameter tempParam = actingThread.getTemporaryParameter();

        //Calculate the movement in this tick
        double xStep = panVx * delta;
        double yStep = panVy * delta;

        //Move
        this.x += xStep;
        this.y += yStep;

        //Successfully moved, update temporary variable
        double movedDistance = Math.sqrt(xStep*xStep + yStep*yStep);
        tempParam.setDoubleValue(
                tempParam.getDoubleValue() -
                        movedDistance);

        //The next line might be redundant? I think...
        actingThread.setTemporaryParameter(tempParam);

        if (tempParam.getDoubleValue() < 0)
        {
            //Oh, so we're done moving. Great.
            actingThread.setRunningState(false);
            return false;
        }

        //Alright, we still have to move. Keep moving.
        return true;

    }

    public void beginPan(double pixelsToMove, Thread actingThread)
    {
        System.out.println("!!" + x + " " +  y);

        //Umm yeah this part isn't so easy because there is something that has to do with
        //the map rendering routine involved here. I need to work harder to figure this out.
        //Also need to have some sort of a center-on function. Anyway, try to figure out
        //Exactly what the viewRect means..
        x = getViewRect().x;
        y = getViewRect().y;
        frozen = true;

        actingThread.setTemporaryParameter(new Parameter(pixelsToMove));
        actingThread.setRunningState(true);
    }

    public void freezeAt(int screenUL_X, int screenUL_Y)
    {

        frozen = true;
        x = screenUL_X;
        y = screenUL_Y;
    }
    
    public void unfreeze()
    {
        frozen = false;
    }
    
    //Center around the player
    public DoubleRect getViewRect()
    {
        if (frozen) return new DoubleRect(
                x,
                y, 
                VoyageQuest.X_RESOLUTION,
                VoyageQuest.Y_RESOLUTION);
        
        //Using the width and height of the player, arrive at the 
        double playerCenterX = VoyageQuest.player.r.getX() + VoyageQuest.player.r.width/2.0;
        double playerCenterY = VoyageQuest.player.r.getY() + VoyageQuest.player.r.height/2.0;
        
        //Now, compensate for the length of the screen
        double startX = playerCenterX - screenCenterX;
        double startY = playerCenterY - screenCenterY;
        
        //Now, check if we have to compensate for going off the map in all directions
        //Over the limit
        if (startX + VoyageQuest.X_RESOLUTION > Global.currentMap.MAP_WIDTH)
        {
            startX = Global.currentMap.MAP_WIDTH - VoyageQuest.X_RESOLUTION;
        }
        if (startY + VoyageQuest.Y_RESOLUTION > Global.currentMap.MAP_HEIGHT)
        {
            startY = Global.currentMap.MAP_HEIGHT - VoyageQuest.Y_RESOLUTION;
        }
        //Tiles less than 0
        if (startX < 0.0d) startX = 0.0d;
        if (startY < 0.0d) startY = 0.0d;
        
        return new DoubleRect(
                startX,
                startY, 
                VoyageQuest.X_RESOLUTION,
                VoyageQuest.Y_RESOLUTION);
    }

    public void display(Graphics g) throws SlickException
    {
        drawMap(g);

        //Darkens screen if necessary
        fading(g);
    }

    private void drawMap(Graphics g)
    {
        //get the rectangle representing the Camera's range of vision
        DoubleRect vRect = getViewRect();

        //The starting tiles of the map to draw
        int startX = (int)(vRect.x/Global.currentMap.TILE_LENGTH);
        int startY = (int)(vRect.y/Global.currentMap.TILE_LENGTH);

        //Compensate for distance between tiles
        int extraX = -(int)(vRect.x % 64);
        int extraY = -(int)(vRect.y % 64);

        //Calculate the number of tiles necessary
        //The + 2 compensates for that extra tile we would otherwise leak
        int tileColumnsNeeded = (VoyageQuest.X_RESOLUTION / Global.currentMap.TILE_LENGTH) + 2;
        int tileRowsNeeded = (VoyageQuest.Y_RESOLUTION / Global.currentMap.TILE_LENGTH) + 2;

        //Draw the bottom layers which are below everything else.
        for (int layer = 0; layer < Global.currentMap.MAP_ENTITY_LAYER; layer++)
        {
            Global.currentMap.tileMap.render(extraX, extraY, startX, startY,
                    tileColumnsNeeded, tileRowsNeeded, layer, false);
        }

        //The deferred list is the list of Entities which must wait...
        LinkedList<Rectangular> drawingDeferrals = new LinkedList<>();
        ListIterator deferIterator = null;

        //Query the region for Rectangulars
        DoubleRect queryRect = this.getViewRect();
        LinkedList<Rectangular> entitiesToConsider = Global.currentMap.collisions.rectQuery(queryRect);

        //Rectangulars include the GroupObjectWrappers which work in collision detection, so let's
        //filter them out.
        ListIterator entityIterator = entitiesToConsider.listIterator();
        while (entityIterator.hasNext())
        {
            Rectangular currentRectangular = (Rectangular)entityIterator.next();
            if (currentRectangular instanceof GroupObjectWrapper)
            {
                entityIterator.remove();
            }
            else
            {
                //Also don't forget to set their rendersettings to null
                ((Entity)currentRectangular).renderSetting = null;
            }
        }


        //The following procedure will associate each Entity with a RenderSetting object which
        //describes which tile it should be rendered in, and...
        for (Rectangular r : entitiesToConsider)
        {
            //We are in fact working with an Entity here, so let's case it now.
            Entity e = (Entity)r;
            DoubleRect entityRect = e.getRect();

            //Get a list of all the BoundaryWrappers this Entity could collide with
            LinkedList<Rectangular> collisionChoices = Global.currentMap.boundaries.rectQuery(entityRect);

            //RenderSetting to be set soon.
            RenderSetting newRenderSetting;

            //Find the first one it actually collides with.
            BoundaryWrapper chosenBoundary = null;
            for (Rectangular check : collisionChoices)
            {
                //If this boundary touches the Entity, then we have our match
                if ( ((BoundaryWrapper)check).getRect().intersects( entityRect ) )
                {
                    chosenBoundary = (BoundaryWrapper)check;
                    //We're done. We found the first thing Entity collides with.
                    break;
                }
            }

            //If chosenBoundary is still null after the process, then we can just
            //insert this entity for drawing and move on to the next entity because
            //we don't have to deal with touching something on the map.
            if (chosenBoundary == null)
            {
                //We are not touching any Entities, so we can just draw this right after
                //the tile its UL corner is on.

                //Find the UL corner's x,y and get the tile it belongs in
                DoubleRect entityUL = Util.coordinateToTile(
                        entityRect.x, entityRect.y);

                newRenderSetting = new RenderSetting((int)entityUL.y, false);
                e.renderSetting = newRenderSetting;

                //Continue on to the next Entity.
                continue;
            }

            //Alright, so we are in contact with something. Let's figure out when
            //to draw Entity.

            //Compare the collisionbox of the entity with the collisionbox in
            //the boundary to determine what should be done
            GroupObjectWrapper boundaryColl = chosenBoundary.getSecondaryGroupObject();
            DoubleRect entityColl = e.getCollRect();

            double boundaryCollY = boundaryColl.getRect().getY();
            double entityCollY = entityColl.getY();

            if (entityCollY >= boundaryCollY)
            {
                //The Entity is ahead of the object, so we want to render right after the
                //lowest Tile of that object.
                DoubleRect setting = chosenBoundary.getLowestTile();
                newRenderSetting = new RenderSetting((int)setting.y, false);
                e.renderSetting = newRenderSetting;

            }
            else
            {
                //When the Entity is behind the object
                DoubleRect setting = chosenBoundary.getTopTile();
                newRenderSetting = new RenderSetting((int)setting.y - 1, true);
                e.renderSetting = newRenderSetting;
            }

        }

        //Perform important calculations
        //EXTRA_ROW_COUNT compensates for an extra tile leaking out
        Layer objLayer = Global.currentMap.tileMap.getLayers().get(
                Global.currentMap.MAP_ENTITY_LAYER);
        int EXTRA_ROW_COUNT = 1;
        int startRow = (int)(vRect.y/64) - EXTRA_ROW_COUNT;
        int endRow = startRow + tileRowsNeeded + EXTRA_ROW_COUNT;
        int rowsDrawn = 0;

        //Now that the preparations are ready, we can now step through, row by row,
        //rendering the Entities.
        for (int i = startRow; i < endRow; i++)
        {
            //Render all objects which are in the row and
            //are supposed to be behind the tile
            entityIterator = entitiesToConsider.listIterator();
            while (entityIterator.hasNext())
            {
                Entity e = (Entity)entityIterator.next();

                if (e.renderSetting.drawnRow == i &&
                        e.renderSetting.drawBefore == true)
                {
                    e.draw(g,
                            (float)(e.r.x - vRect.getX()),
                            (float)(e.r.y - vRect.getY())
                    );
                    entityIterator.remove();
                }
            }

            //=======================/RENDER THIS ROW/=========================

            //rowsDrawn*Global.current.TILE_LENGTH adjusts based on row
            //extraY adjusts for the space between tiles
            //-EXTRA_ROW_COUNT*Global.currentMap.TILE_LENGTH compensates for the extra row
            int objYScreenPosition =
                    rowsDrawn*Global.currentMap.TILE_LENGTH +
                            extraY -
                            EXTRA_ROW_COUNT*Global.currentMap.TILE_LENGTH;

            objLayer.render(extraX,
                    objYScreenPosition,
                    startX,
                    i,
                    tileColumnsNeeded,
                    1,
                    false,
                    Global.currentMap.TILE_LENGTH,
                    Global.currentMap.TILE_LENGTH);
            rowsDrawn++;

            //Render all objects which are in the row and
            //are supposed to be ahead of the tile
            entityIterator = entitiesToConsider.listIterator();
            while (entityIterator.hasNext())
            {
                Entity e = (Entity)entityIterator.next();

                if (e.renderSetting.drawnRow == i &&
                        e.renderSetting.drawBefore == false)
                {
                    e.draw(g,
                            (float)(e.r.x - vRect.getX()),
                            (float)(e.r.y - vRect.getY())
                    );
                    entityIterator.remove();
                }
            }
        }

        //Draw the layers which tower above the entities
        int layerAfterEntities = Global.currentMap.MAP_ENTITY_LAYER + 1;
        int totalLayers = Global.currentMap.MAP_TILE_LAYERS;
        for (int layer = layerAfterEntities; layer < totalLayers; layer++)
        {
            Global.currentMap.tileMap.render(extraX, extraY, startX, startY,
                    tileColumnsNeeded, tileRowsNeeded, layer, false);
        }

        //When in debug mode, draw gridlines to assist placement and debugging
        if (VoyageQuest.DEBUG_MODE == true)
        {
            //Draw the tile borders
            g.setColor(Color.black);
            g.setLineWidth(1.0f);

            //Draw the lines parallel to x axis
            for (int i = 0; i < tileRowsNeeded; i++)
            {
                float yCoord = (float)(i * Global.currentMap.TILE_LENGTH + extraY);
                g.drawLine(0.0f, yCoord, VoyageQuest.X_RESOLUTION, yCoord);
                Util.FONT.drawString(0, i * Global.currentMap.TILE_LENGTH + extraY + 30, "Row " + (startY + i));
            }

            //Draw the lines parallel to y axis
            for (int i = 0; i < tileColumnsNeeded; i++)
            {
                float xCoord = (float)(i * Global.currentMap.TILE_LENGTH + extraX);
                g.drawLine(xCoord, 0, xCoord, VoyageQuest.Y_RESOLUTION);

            }

            //The partitions help me debug, so draw these too
            drawPartitionBoxes(g);

            //Draw the awkward boundary boxes too
            g.setColor(Color.red);
            LinkedList<Rectangular> entList = Global.currentMap.boundaries.rectQuery(vRect);
            for (Rectangular e : entList)
            {
                //It was a GroupObjectWrapper...
                g.setLineWidth(2.0f);
                DoubleRect ourRect = e.getRect();
                g.drawRect(
                        (float)(ourRect.x - vRect.x),
                        (float)(ourRect.y - vRect.y),
                        (float)(ourRect.width),
                        (float)(ourRect.height));

                //Draw its corresponding Boundary Wrapper
                GroupObjectWrapper asdf = ((BoundaryWrapper)e).getSecondaryGroupObject();
                g.setColor(Color.black);
                g.setLineWidth(2.0f);
                ourRect = asdf.getRect();
                g.drawRect(
                        (float)(ourRect.x - vRect.x),
                        (float)(ourRect.y - vRect.y),
                        (float)(ourRect.width),
                        (float)(ourRect.height));
            }

        }
    }

    /*
     * This technique I gained from the slick2D forums - Edmund
     * The idea is to draw to the alpha channel.
     * Basically, drawing black makes things light
     * Drawing white makes things dark.
     */
    private void fading(Graphics g) {

        //Scale the light map down so it can be a small light map
        float invSizeX = 1f / 20;
        float invSizeY = 1f / 15;
        g.scale(20, 15);

        //Setting alpha channel ready
        g.clearAlphaMap();
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);

        if (lightSourceOn)
            lightSource(g);

        //The faded variable... the color is black, but the alpha channel basically changes,
        //Taking the color from black, to grey, to white. As a result because of the alpha channel
        //the game environment grows from bright to dark.
        //255 --> 0
        g.setColor(new Color(0, 0, 0, fade));

        //#faded
        g.fill(new Rectangle(0, 0, VoyageQuest.X_RESOLUTION * invSizeX,
                VoyageQuest.Y_RESOLUTION * invSizeY));

        //Now scale the light map up again
        g.scale(invSizeX, invSizeY);

        //Setting alpha channel for clearing everything but light map just added
        GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_DST_ALPHA);

        //Paint everything else with black
        g.fillRect(0, 0, VoyageQuest.X_RESOLUTION, VoyageQuest.Y_RESOLUTION);

        //Setting drawing mode back to normal
        g.setDrawMode(Graphics.MODE_NORMAL);

    }

    private void lightSource(Graphics g)
    {
        //Scale the light map down so it can be a small light map
        float invSizeX = 1f / 20;
        float invSizeY = 1f / 15;

        //This light map works because of reasons stated below - it is like a light source,
        //revealing a part of the map where the AlphaMap image is dark. Black = light, basically
        caveLight.drawCentered(VoyageQuest.X_RESOLUTION/2 * invSizeX,
                VoyageQuest.Y_RESOLUTION/2 * invSizeY);

    }



    public void drawPartitionBoxes(Graphics g)
    {
        //The viewrect represents where in the map to start drawing from, so we can subtract this
        //rectangle's x and y values from an entity's x and y values in order to get the location on
        //the screen to draw them.
        DoubleRect dr = getViewRect();
                
        g.setColor(org.newdawn.slick.Color.yellow);
        
        LinkedList<TreeNode> partitionBoxes = Global.currentMap.collisions.getPartitions();
        for (TreeNode t : partitionBoxes)
        {
            DoubleRect r = t.boundary;
            g.drawRect(
                    (float)(r.x - dr.x),
                    (float)(r.y - dr.y),
                    (float)(r.width),
                    (float)(r.height));
            
        }
        
    }
    
}