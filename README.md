# [![Build Status](https://travis-ci.org/gbasler/barrier-option.png?branch=master)](https://travis-ci.org/gbasler/barrier-option) Calculation of an option price using a binomial tree
A short introduction from a computer science perspective.

## Idea

In order to calculate the price of an option there exists the
famous Black-Scholes formula which relates the price of an option
to its volatility, stock price, strike and time to expiry.
It looks complicated and is not intuitive. It's also important to realize that this is - unlike the laws of physics -
just a model and can be arbitrary far away from reality.

However there's a concept which has the same assumption but is far easier to understand intuitively:
the binomial tree model. Computationally probably the worst choice (O(N^2)) but
let's ignore that here (a good analogy here is bubble-sort: it's super easy to understand
but should never be used in practice since it's probably the worst choice.). 

One important assumption in option pricing is the no arbitrage principle:
it's not possible to make money out of thin air. That means no matter how someone
invests his money, every investment must deliver the same profit. Otherwise one could
make a profit by buying one and short selling the other. That assumption is only true if
every investment has the same risk (which is assumed to be zero). This is also referred to as
risk-neutral pricing. Using this assumption, every investment just grows with the risk-free interest rate.

The one-period model looks like:

                 S1u = u * S0
               /   
              / qu   
             /   
        S0
             \ 
              \ qd
               \ 
                 S1d = d * S0

`qu` and `qd` are the probabilities of up / down move, and `u` / `d` are the factors by which the share price `S0` changes.

In the risk neutral world the stock price `S0` grows in one period with the risk free interest rate, thus `S1 = S0 * (1 + r)`.
Using the risk-neutral probabilities `qu` and `qd` the expected stock price is also `S1 = S0 * qu * u + S0 * qd * d`.
Thus one gets the equation `(1 + r) = qu * u + qd * d` using the fact that probatilities sum up to one, one can obtain formulas for `qu` and `qd`:

             (1 + r) - d         u - (1 + r) 
        qu = -----------,   qd = -----------
                u - d              u - d
          
## Pricing Algorithm

The extension of the model to multiple periods is straightforward. If we set ´d/1/u´, we get a recombining tree, i.e., the intermediate nodes merge and we get one node more for each time step instead of double the number of nodes.

The pricing algorithm proceeds in three steps: 

* Construct the binomial tree forward in time: calculate for each node the stock price
* Evaluate the payoff at the terminal nodes using the call / put formula: `max(0, S - K)` / `max(0, K - S)`
* Propagate the values of the nodes back (i.e., from the terminal nodes to the initial node)

Note that in order to save memory and computation time, not the whole tree has to be stored. The stock price at the terminal
nodes can be directly computed and stored in an array. When we go back in time, we can keep updating this array.

## Stock price at intermediate nodes

If the node numbering starts with `(0,0)` for the inital node and `(i, k)` denotes the node after i time steps and k up steps.
The stock price can be directly computed: let's just look at the lowest tree node, in each time step we go down one step, so we multiply with `d^i`. Then we just go up `k` steps, in order to do that, we undo one down step and go up one step, thus we multiply with `u^2k`:
   
    S0 * pow(d, i) * pow(u, 2*k) =(using d=1/u) pow(u, 2*k - i)    
Thus

    S0 * pow(u, 2 * k - i)

The [wikipedia page](https://en.wikipedia.org/wiki/Binomial_options_pricing_model) contains a lot of information but it misses some points:

If we go from `t+1` to `t` back in time the value has to be discounted with `exp(r*T/N)` (N = number of steps) after each node.
Alternatively we can discount the final value with `exp(-r*T)`. If we price a barrier option or an American option it's simpler to just discount the final value.


# Pricing of barrier options

Barrier options are path dependent options. 
Let's assume we price a down-and-in put option. If there's just one discrete barrier observation date, we can just compare the stock at that node with the barrier level and propagate a zero instead of the node value back. That seems pretty simple and it's possible to price barrier options with just one discrete date with an Analytical formula <sup>[1](#myfootnote1)</sup>.

However, if there's more than one discrete observation date, the problem get's trickier. Assume that we have two observation dates and repeat our idea: for each date, check if the stock price is higher (i.e. no knock in) as the barrier level and propagate zero in that case. If we do that, we demand that there's a knock-in event at both dates, which is not what we wanted. It would be sufficient if the stock price is lower than the barrier level at one of these dates. 

One solution to this problem is to start counting the number of paths that can be taken to arrive at the terminal nodes.
An very readable explanation of this idea can be found [here](https://repository.tudelft.nl/islandora/object/uuid:63f833e0-d0ff-4615-ad23-e8aa1d148188?collection=education).

## The code
If there are just two barrier observation dates, we can also split the problem into sub problems:
 * Take all paths that knock in at the last observation date and use the usual algorithm to propagate these values back
 * Take all paths that do not knock in at the last observation date, and propagate them back with a check at the first observation barrier observation date.

I have implemented this in the main file, together with some Specs2 tests. If the barrier level is set to a very high value,
then each path is knocked in and the price is the same as obtained from the Black-Scholes formula.

The data is read from a file `barrier.txt`, which looks like:

    CalculationDate 10.02.2017
    Volatility 0.2
    Dividends 0
    S0 100
    Strike 100
    Barrier 70
    ObservationDate1 10.08.2017
    ObservationDate2 10.02.2018
    Rate 0.0
    Expiry 10.02.2018

Note that I haven't implemented support for a dividend yield but it should be straightforward to add since you can just subtract
the rate from the risk free interest rate.

If you run `BarrierOptionMain`, you'll get the price for the option specified above:
`1.695`. I have double checked the result with a Monte-Carlo simulator which
produced `1.668` (16 million samples). The result seems close but I would have expected
a closer match.

### References

<a name="myfootnote1">1</a>: `Peter Buchen, An Introduction to Exotic Option Pricing` describes Analytical formula for many option types. However, the barrier option with one observation requires a `joint distribution` (referred to multivariate normal distribution in Apache commons math). What the book does not tell you is that you need numerical integration to calculate the cumulative density.

[2] [NineWays to Implement the Binomial Method for Option Valuation in MATLAB](http://epubs.siam.org/doi/pdf/10.1137/S0036144501393266) Contains a bunch of Matlab files which compute prices via binomial trees
in different ways. Good source to learn about different implementation alternatives and details. 
