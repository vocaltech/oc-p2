# Streams

## Lambda compact notation

 ```
 BigDecimal grandTotal = list.stream()
        .sorted((a, b) -> b.getDate().compareTo(a.getDate()))
        .peek(e -> System.out.println(e.toCsvLine()))
        .map(Expense::getAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
 ```
        
## Lambda expanded notation
```
BigDecimal grandTotal = list.stream()
        .sorted((a, b) -> b.getDate().compareTo(a.getDate()))
        .peek(e -> System.out.println(e.toCsvLine()))
        .map(e -> e.getAmount())
        .reduce(BigDecimal.ZERO, (acc, amount) -> acc.add(amount));
 ```
