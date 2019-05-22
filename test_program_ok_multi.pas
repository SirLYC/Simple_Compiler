begin
  integer m;
  integer k;
  integer function F(n);
    begin
      integer n;
      if n<=0 then F:=1
      else F:=n*F(n-1)
    end;
  integer function F1(n);
begin
  integer n;
  integer function F(n);
begin
  integer n;
end;
end;
  read(m);
  k := F(m);
  write(k)
end
