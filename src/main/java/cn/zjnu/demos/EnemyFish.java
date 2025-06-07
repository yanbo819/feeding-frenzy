package cn.zjnu.demos;

import javafx.scene.canvas.GraphicsContext;

class EnemyFish extends Fish {
    private boolean facingRight; 

    public EnemyFish(double x, double y, int sizeIndex) {
        super(x, y, sizeIndex); 

        facingRight = (dx >= 0);
    }

    @Override
    public void move() {
        super.move();
       
        if (dx > 0 && !facingRight) {
            facingRight = true;
        } else if (dx < 0 && facingRight) {
            facingRight = false;
        }
    }

    @Override
    public void render(GraphicsContext gc) {
        if (alive && image != null) { 
            gc.save(); 

            if (!facingRight) {
                
                gc.translate(x + size / 2, y + size / 2); 
                gc.scale(-1, 1); 
                gc.translate(-(x + size / 2), -(y + size / 2)); 
            }
            
            gc.drawImage(image, x, y, size, size);
            gc.restore(); 
        }
    }
}