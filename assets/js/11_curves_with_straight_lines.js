let A, B, C; // Vertices of the original outer triangle
let p1, p2, p3; // Current vertices of the inward-crawling triangle
let t = 0.03; // Interpolation factor - how far inward each iteration moves (0.03 = 3%)
let frame = 0; // Frame counter to track which edge to draw

function setup() {
  createCanvas(windowWidth, windowHeight);
  background("ffffff");
  stroke(0);
  noFill();
  frameRate(60);

  setupTriangle();
}

function setupTriangle() {
  // Use the smaller dimension to ensure triangle fits
  let size = min(width, height) * 0.8; // Base width of the triangle (80% of smaller dimension)

  // Calculate equilateral triangle height
  let triangleHeight = size * (sqrt(3) / 2); // Height formula for equilateral triangle

  // Center the triangle horizontally, but lower it vertically
  let centerX = width / 2; // Horizontal center of canvas
  let centerY = height / 2 + height * 0.1; // Vertical center, shifted down by 10%

  // Define vertices for centered equilateral triangle
  A = createVector(centerX - size / 2, centerY + triangleHeight / 3); // Bottom-left vertex
  B = createVector(centerX + size / 2, centerY + triangleHeight / 3); // Bottom-right vertex
  C = createVector(centerX, centerY - (2 * triangleHeight) / 3); // Top vertex

  // Draw initial triangle outline
  line(A.x, A.y, B.x, B.y);
  line(B.x, B.y, C.x, C.y);
  line(C.x, C.y, A.x, A.y);

  // Initialize triangle vertices for inward crawl
  p1 = A.copy(); // Start at vertex A
  p2 = B.copy(); // Start at vertex B
  p3 = C.copy(); // Start at vertex C
}

function draw() {
  let edge = frame % 3; // Determine which edge to draw (0, 1, or 2)

  // Compute the next inward-shifted triangle vertices
  let newA = p5.Vector.lerp(p1, p2, t); // Move from p1 toward p2
  let newB = p5.Vector.lerp(p2, p3, t); // Move from p2 toward p3
  let newC = p5.Vector.lerp(p3, p1, t); // Move from p3 toward p1

  // Draw only one side of the current triangle per frame
  if (edge === 0) line(p1.x, p1.y, p2.x, p2.y); // Draw edge from p1 to p2
  if (edge === 1) line(p2.x, p2.y, p3.x, p3.y); // Draw edge from p2 to p3
  if (edge === 2) line(p3.x, p3.y, p1.x, p1.y); // Draw edge from p3 to p1

  // Every 3 frames (after all edges drawn), move inward to the new triangle
  if (edge === 2) {
    p1 = newA;
    p2 = newB;
    p3 = newC;
  }

  // Stop when triangle gets too small (distance between vertices < 1 pixel)
  if (p5.Vector.dist(p1, p2) < 1) noLoop();
  frame++;
}

function windowResized() {
  resizeCanvas(windowWidth, windowHeight); // Adjust canvas to new window size
  background(220); // Clear the canvas
  frame = 0; // Reset frame counter
  setupTriangle(); // Recalculate and redraw triangle
  loop(); // Restart the animation
}
