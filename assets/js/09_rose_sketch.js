var n = 1.5; // Start somewhere between first and second pattern
var d = 1;
var nTarget = 2;
var dTarget = 1;
var transitionSpeed = 0.0005;

function setup() {
  createCanvas(windowWidth, windowHeight);
}

function draw() {
  background("#ffffff");
  translate(width/2, height/2);
  
  // Update target values every 2 seconds (120 frames at 60fps)
  updateTargets();
  
  // Smoothly interpolate n and d towards their targets
  n = lerp(n, nTarget, transitionSpeed);
  d = lerp(d, dTarget, transitionSpeed);
  
  // Calculate k
  var k = n/d;
  
  // Draw the rose
  beginShape();
  noFill();
  stroke("#000000");
  strokeWeight(3);
  
  for (var a = 0; a < TWO_PI * 10; a += 0.02) {
    var r = 150 * cos(k * a);
    var x = r * cos(a);
    var y = r * sin(a);
    
    vertex(x, y);
  }
  
  endShape();
}

function updateTargets() {
  // All unique n/d combinations from your ranges (1-7, 1-9)
  // Excluding n=d cases and duplicates (same reduced fraction)
  var combinations = [
    // n/1 ratios (basic roses)
    {n: 2, d: 1}, {n: 3, d: 1}, {n: 4, d: 1}, {n: 5, d: 1}, {n: 6, d: 1}, {n: 7, d: 1},
    
    // n/2 ratios
    {n: 1, d: 2}, {n: 3, d: 2}, {n: 5, d: 2}, {n: 7, d: 2},
    // Note: (2,2), (4,2)=2/1, (6,2)=3/1 are excluded as duplicates
    
    // n/3 ratios  
    {n: 1, d: 3}, {n: 2, d: 3}, {n: 4, d: 3}, {n: 5, d: 3}, {n: 7, d: 3},
    // Note: (3,3), (6,3)=2/1 are excluded
    
    // n/4 ratios
    {n: 1, d: 4}, {n: 3, d: 4}, {n: 5, d: 4}, {n: 7, d: 4},
    // Note: (2,4)=1/2, (4,4), (6,4)=3/2 are excluded
    
    // n/5 ratios
    {n: 1, d: 5}, {n: 2, d: 5}, {n: 3, d: 5}, {n: 4, d: 5}, {n: 6, d: 5}, {n: 7, d: 5},
    // Note: (5,5) excluded
    
    // n/6 ratios
    {n: 1, d: 6}, {n: 5, d: 6}, {n: 7, d: 6},
    // Note: (2,6)=1/3, (3,6)=1/2, (4,6)=2/3, (6,6) are excluded
    
    // n/7 ratios
    {n: 1, d: 7}, {n: 2, d: 7}, {n: 3, d: 7}, {n: 4, d: 7}, {n: 5, d: 7}, {n: 6, d: 7},
    // Note: (7,7) excluded
    
    // n/8 ratios
    {n: 1, d: 8}, {n: 3, d: 8}, {n: 5, d: 8}, {n: 7, d: 8},
    // Note: (2,8)=1/4, (4,8)=1/2, (6,8)=3/4 are excluded
    
    // n/9 ratios
    {n: 1, d: 9}, {n: 2, d: 9}, {n: 4, d: 9}, {n: 5, d: 9}, {n: 7, d: 9}
    // Note: (3,9)=1/3, (6,9)=2/3 are excluded
  ];
  
  // Calculate which combination to use based on frameCount
  var currentIndex = floor(frameCount / 120) % combinations.length;
  
  nTarget = combinations[currentIndex].n;
  dTarget = combinations[currentIndex].d;
}