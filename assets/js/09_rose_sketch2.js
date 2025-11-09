function sketch(p) {
  let n = 1.5; // Start somewhere between the first and second pattern
  let d = 1;
  let nTarget = 2;
  let dTarget = 1;
  let transitionSpeed = 0.0005;

  let hexColors = [
    "#264653", // deep teal
    "#2a9d8f", // turquoise green
    "#e9c46a", // warm mustard yellow
    "#f4a261", // muted orange
    "#e76f51", // warm coral red
  ];

  let colors = [];

  let colorTransitionDuration = 180; // frames per color transition (~3 seconds at 60fps)

  p.setup = function () {
    let parentEl = document.getElementById("rose2");
    p.createCanvas(p.windowWidth, p.windowHeight, p.P2D, parentEl);
    // Convert hex strings to p5.Color objects once
    colors = hexColors.map((c) => p.color(c));
  };

  p.draw = function () {
    p.background("#ffffff");
    p.translate(p.width / 2, p.height / 2);

    // Update target values and interpolate
    updateTargets();
    n = p.lerp(n, nTarget, transitionSpeed);
    d = p.lerp(d, dTarget, transitionSpeed);

    // Calculate rose constant
    let k = n / d;

    // Calculate interpolation progress (0 to 1) within current color transition
    let t = (p.frameCount % colorTransitionDuration) / colorTransitionDuration;

    // Current and next color indices in palette
    let currentIndex =
      p.floor(p.frameCount / colorTransitionDuration) % colors.length;
    let nextIndex = (currentIndex + 1) % colors.length;

    // Interpolate between the two colors
    let currentColor = p.lerpColor(colors[currentIndex], colors[nextIndex], t);

    p.stroke(currentColor);
    p.strokeWeight(1.75);
    p.noFill();

    // Draw the rose curve with smooth curvature
    p.beginShape();
    for (let a = 0; a < p.TWO_PI * 10; a += 0.02) {
      let r = 250 * p.cos(k * a);
      let x = r * p.cos(a);
      let y = r * p.sin(a);
      p.curveVertex(x, y);
    }
    p.endShape();
  };

  function updateTargets() {
    // Valid n/d combinations
    let combinations = [
      { n: 2, d: 1 },
      { n: 3, d: 1 },
      { n: 4, d: 1 },
      { n: 5, d: 1 },
      { n: 6, d: 1 },
      { n: 7, d: 1 },
      { n: 1, d: 2 },
      { n: 3, d: 2 },
      { n: 5, d: 2 },
      { n: 7, d: 2 },
      { n: 1, d: 3 },
      { n: 2, d: 3 },
      { n: 4, d: 3 },
      { n: 5, d: 3 },
      { n: 7, d: 3 },
      { n: 1, d: 4 },
      { n: 3, d: 4 },
      { n: 5, d: 4 },
      { n: 7, d: 4 },
      { n: 1, d: 5 },
      { n: 2, d: 5 },
      { n: 3, d: 5 },
      { n: 4, d: 5 },
      { n: 6, d: 5 },
      { n: 7, d: 5 },
      { n: 1, d: 6 },
      { n: 5, d: 6 },
      { n: 7, d: 6 },
      { n: 1, d: 7 },
      { n: 2, d: 7 },
      { n: 3, d: 7 },
      { n: 4, d: 7 },
      { n: 5, d: 7 },
      { n: 6, d: 7 },
      { n: 1, d: 8 },
      { n: 3, d: 8 },
      { n: 5, d: 8 },
      { n: 7, d: 8 },
      { n: 1, d: 9 },
      { n: 2, d: 9 },
      { n: 4, d: 9 },
      { n: 5, d: 9 },
      { n: 7, d: 9 },
    ];

    // Cycle through combinations every 120 frames (~2 seconds at 60fps)
    let currentIndex = p.floor(p.frameCount / 120) % combinations.length;
    nTarget = combinations[currentIndex].n;
    dTarget = combinations[currentIndex].d;
  }
}

new p5(sketch);
