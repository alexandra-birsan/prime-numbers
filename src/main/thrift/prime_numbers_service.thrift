namespace * primenumbers.thrift

service PrimeNumbersService {

  list<i32> getPrimeNumbers(1: i32 n);

}