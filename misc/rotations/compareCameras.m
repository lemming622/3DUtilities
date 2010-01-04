function compareCameras(folder, method)
%Compare the recovered camera parameters to the ground truth

%Load ground truth values (assumes every K has an RT next)
fid = fopen([folder 'cameras.txt.gt'], 'r');
K0 = {};
RT0 = {};
R0 = {};

while(~feof(fid))
	line = fgetl(fid);
	tmp = split(line);

	if(tmp{2} == 'i')				%Assume parameters
		tmp = {tmp{4:7}};
	  K = zeros(4,4);

		K(1,1) = str2double(tmp{1});
		K(2,2) = str2double(tmp{2});
		K(1,3) = str2double(tmp{3});
		K(2,3) = str2double(tmp{4});
		K(3,3) = 0;
	  K(3,4) = 0;
		K(4,3) = 1;

		K0{length(K0)+1} = K;
	elseif(tmp{2} == 'e')		%Assume matrix values
		tmp = {tmp{4:19}};
		RT = zeros(4,4);

		for(i=1:length(tmp))
			RT(i) = str2double(tmp{i});
		end

		RT = RT';
		RT0{length(RT0)+1} = RT;
		R0{length(R0)+1} = RT(1:3,1:3);
	end
end

fclose(fid);

%Load reconstructed values (assumes every K has an RT next)
fid = fopen([folder 'cameras.txt'], 'r');
K1 = {};
RT1 = {};
R1 = {};

while(~feof(fid))
	line = fgetl(fid);
	tmp = split(line);

	if(tmp{2} == 'i')				%Assume parameters
		tmp = {tmp{4:7}};
	  K = zeros(4,4);

		K(1,1) = str2double(tmp{1});
		K(2,2) = str2double(tmp{2});
		K(1,3) = str2double(tmp{3});
		K(2,3) = str2double(tmp{4});
		K(3,3) = 0;
	  K(3,4) = 0;
		K(4,3) = 1;

		K1{length(K1)+1} = K;
	elseif(tmp{2} == 'e')		%Assume matrix values
		tmp = {tmp{4:19}};
		RT = zeros(4,4);

		for(i=1:length(tmp))
			RT(i) = str2double(tmp{i});
		end

		RT = RT';
		RT1{length(RT1)+1} = RT;
		R1{length(R1)+1} = RT(1:3,1:3);
	end
end

fclose(fid);

%Load axis points
fid = fopen([folder 'axis_points.txt'], 'r');
P = {};
p = {};

while(~feof(fid))
	line = fgetl(fid);
  M = [];
  m = [];	

	while(~isempty(line))
		tmp = split(line);

	  i = size(M,1)+1;
		M(i,:) = [str2double(tmp{1}) str2double(tmp{2}) str2double(tmp{3})];
		m(i,:) = [str2double(tmp{4}) str2double(tmp{5})];

		line = fgetl(fid);
  end

	if(~isempty(M))
		P{length(P)+1} = M;
		p{length(p)+1} = m;
	end
end

fclose(fid);

if(method == 1)		%Compare axis in 3D
	for(i=1:length(R0))
		figure(i);
		plotAxis(R0{i}, R1{i});
	end
else		%Compare re-projections in 2D
	for(i=1:length(P))
		%Draw ground truth image points
		figure(i); clf;
		plot(p{i}(:,2), p{i}(:,1), 'r.');

		%Draw reconstructed image points
		K = K1{i};
		RT = RT1{i};
		KRT = K * RT;

		scale = 150;		%The scale of the axis
    Pi = [scale*P{i} ones(size(P{i},1),1)]';
		pi = KRT * Pi;
	  pi = pi';

		for(j=1:size(pi,1))
			pi(j,:) = pi(j,:) ./ pi(j,4);
		end

		hold on;
		plot(pi(:,2), pi(:,1), 'bs');
		hold off;
	end
end
