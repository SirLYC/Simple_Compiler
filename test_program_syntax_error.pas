integer k;
  integer k;
  integer function F(n);

      integer n;
      if n<=0 then F:=1
      else F:=n*F(n-1)
    end;
  read(m);
k:=F1(m);
  write(k)
end
