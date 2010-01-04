function R=x2R(x)
%Convert a 1 by 6 array representing the first two rows of a rotation
% matrix into a full 3 by 3 rotation matrix

r1 = x(1:3);
r2 = x(4:6);
r3 = cross(r1, r2);

r1 = r1 / norm(r1);
r2 = r2 / norm(r2);
r3 = r3 / norm(r3);

R = [r1; r2; r3];

if(1)
  R(1,3) = -R(1,3);
	R(2,1) = -R(2,1);
	R(2,2) = -R(2,2);
	R(3,3) = -R(3,3);
end
