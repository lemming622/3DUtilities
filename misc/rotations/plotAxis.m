function plotAxis(arg1, arg2, arg3, arg4, arg5, arg6)
%Plot axis' representing rotations
	
x1 = 0;
y1 = 0;
z1 = 0;
R1 = eye(3);

x2 = 0;
y2 = 0;
z2 = 0;
R2 = eye(3);

if(nargin == 2)
	R1 = arg1;
	R2 = arg2;
elseif(nargin == 4)
	x1 = arg1 * pi/180;
	y1 = arg2 * pi/180;
	z1 = arg3 * pi/180;
	R2 = arg4;
  
	Rx = [1 0 0; 0 cos(x1) sin(x1); 0 -sin(x1) cos(x1)];
  Ry = [cos(y1) 0 -sin(y1); 0 1 0; sin(y1) 0 cos(y1)];
  Rz = [cos(z1) sin(z1) 0; -sin(z1) cos(z1) 0; 0 0 1];
  R1 = Rx*Ry*Rz
elseif(nargin == 6)
	x1 = arg1 * pi/180;
	y1 = arg2 * pi/180;
	z1 = arg3 * pi/180;
	x2 = arg4 * pi/180;
	y2 = arg5 * pi/180;
	z2 = arg6 * pi/180;
	
	Rx = [1 0 0; 0 cos(x1) sin(x1); 0 -sin(x1) cos(x1)];
  Ry = [cos(y1) 0 -sin(y1); 0 1 0; sin(y1) 0 cos(y1)];
  Rz = [cos(z1) sin(z1) 0; -sin(z1) cos(z1) 0; 0 0 1];
  R1 = Rx*Ry*Rz;
	
	Rx = [1 0 0; 0 cos(x2) sin(x2); 0 -sin(x2) cos(x2)];
  Ry = [cos(y2) 0 -sin(y2); 0 1 0; sin(y2) 0 cos(y2)];
  Rz = [cos(z2) sin(z2) 0; -sin(z2) cos(z2) 0; 0 0 1];
  R2 = Rx*Ry*Rz;
end

po1 = R1*[0 0 0]';
px1 = R1*[1 0 0]';
py1 = R1*[0 1 0]';
pz1 = R1*[0 0 1]';

po2 = R2*[0 0 0]';
px2 = R2*[1 0 0]';
py2 = R2*[0 1 0]';
pz2 = R2*[0 0 1]';

clf;
hold on;
h = plot3([po1(1) px1(1)], [po1(2) px1(2)], [po1(3) px1(3)], 'r-');
set(h, 'LineWidth', 4);
h = plot3([po1(1) py1(1)], [po1(2) py1(2)], [po1(3) py1(3)], 'g-');
set(h, 'LineWidth', 4);
h = plot3([po1(1) pz1(1)], [po1(2) pz1(2)], [po1(3) pz1(3)], 'b-');
set(h, 'LineWidth', 4);
h = plot3([po2(1) px2(1)], [po2(2) px2(2)], [po2(3) px2(3)], 'm-');
set(h, 'LineWidth', 4);
h = plot3([po2(1) py2(1)], [po2(2) py2(2)], [po2(3) py2(3)], 'y-');
set(h, 'LineWidth', 4);
h = plot3([po2(1) pz2(1)], [po2(2) pz2(2)], [po2(3) pz2(3)], 'c-');
set(h, 'LineWidth', 4);
hold off;
axis equal;
axis vis3d;
rotate3dC on;

