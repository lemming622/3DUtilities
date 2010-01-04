function debugRotations(arg1, arg2, arg3)
%Test euler angle recovery from a rotation matrix.

clc;
x = 0;
y = 0;
z = 0;
R = eye(3);

if(nargin == 3)
	x = arg1;
	y = arg2;
	z = arg3;
else
	x =  27;
	y = 130;
	z =  58;
end

x = x * pi/180;
y = y * pi/180;
z = z * pi/180;

if(nargin == 1)
	R = arg1;
else
  Rx = [1 0 0; 0 cos(x) sin(x); 0 -sin(x) cos(x)];
  Ry = [cos(y) 0 -sin(y); 0 1 0; sin(y) 0 cos(y)];
  Rz = [cos(z) sin(z) 0; -sin(z) cos(z) 0; 0 0 1];
  R = Rx*Ry*Rz;
end

a = R(1,1);
b = R(1,2);
c = R(1,3);
d = R(2,1);
e = R(2,2);
f = R(2,3);

%Test rotation matrix properties
n1 = norm(R(1,:));
n2 = norm(R(2,:));
r3 = cross(R(1,:), R(2,:));

fprintf('norm(r1) = %0.2f\n', n1);
fprintf('norm(r2) = %0.2f\n', n2);
fprintf('cross(r1,r2) = [%0.4f, %0.4f, %0.4f] -> [%0.4f, %0.4f, %0.4f]\n\n', R(3,:), r3);

%Test equalities
sx_gt = sin(x);
sy_gt = sin(y);
sz_gt = sin(z);
cx_gt = cos(x);
cy_gt = cos(y);
cz_gt = cos(z);

sx = sqrt((-f*(b*e+a*d))/(c*(a^2+b^2)));
sy = c;
sz = (b/f)*sx;
cx = ((a*c)/b)*sx + (d*f)/(b*sx); 
cz = (-a/f)*sx;
cy = a / cz;
tz = -b / a;

fprintf('sx: %0.2f -> %0.2f\n', sx_gt, sx);
fprintf('sy: %0.2f -> %0.2f\n', sy_gt, sy);
fprintf('sz: %0.2f -> %0.2f\n', sz_gt, sz);
fprintf('cx: %0.2f -> %0.2f\n', cx_gt, cx);
fprintf('cy: %0.2f -> %0.2f\n', cy_gt, cy);
fprintf('cz: %0.2f -> %0.2f\n', cz_gt, cz);
fprintf('tz: %0.2f -> %0.2f\n\n', tan(z), tz);

%Test angles
xc = acos(cx) * 180/pi;
xs = asin(sx) * 180/pi;
fprintf('x: %0.2f -> %0.2f, %0.2f\n', x*180/pi, xc, xs);

yc = acos(cy) * 180/pi;
ys = asin(sy) * 180/pi;
fprintf('y: %0.2f -> %0.2f, %0.2f\n', y*180/pi, yc, ys);

zc = acos(cz) * 180/pi;
zs = asin(sz) * 180/pi;
zt = atan(tz) * 180/pi;
fprintf('z: %0.2f -> %0.2f, %0.2f, %0.2f\n', z*180/pi, zc, zs, zt);

