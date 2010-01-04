function A=split(line)
%Split a string on spaces

A = {};
ind = [0 find(isspace(line)) length(line)+1];

for(i=1:length(ind)-1)
  if(ind(i)~=(ind(i+1)-1))
		A{length(A)+1} = line(ind(i)+1:ind(i+1)-1);
	end
end

