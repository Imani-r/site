```clojure
{:some "metadata"}
```

# How Common is Your Birthday? UK Edition

This project began with a realization that there seem to be so many birthdays in September. Is it possible that September is the most common birthday month? I then began wondering about factors that influence conception time. The obvious guess is that most babies are conceived when it's cold outside, but what in about places where there are no distinct seasons? I sensed a fun puzzle here, so off I went digging to find out more.

I began by looking through the Tableau Public database to see if anyone had done a visualization on this topic. I found that while there are a good number visualizations done on birthdays **[1]**, most used only US data. I thought it might be interesting for me visualize birthday data for Kenya, my home country, or the UK, where I currently reside. Or even better: both, then compare and contrast my findings. Unfortunately, I couldn't find any reliable data for Kenya, so I decided to focus solely on the UK.

Check out what I found below. You can interact with the full visualization on Tableau Public [here](https://public.tableau.com/app/profile/faith5698/viz/HowcommonisyourBirthdayUKEdition/BirthdaysintheUK).

It turns out that September is indeed the most common month for birthdays in the UK, with September 26, 23, and 30 being the top three birthdays. [September is also the top birthday month in the US.](https://www.vizwiz.com/2012/05/how-common-is-your-birthday-find-out.html)

I was surprised to learn that the bottom three birthdays were on holidays: Dec 26, Dec 15, and Jan 1. Is it possible that people deliberately plan pregnancies such that births fall don't fall on a holiday? Or is it more likely that births, to the extent that it's possible, are avoided on holidays e.g. via inducing a few days before?

I also found that December is the most common month for conception, with 26 of the top 30 estimated conception dates **[2]** falling in that month. This must explain all those September birthdays! Now, I wonder why so many babies are conceived in December. Maybe it's the magic of the holidays: we're off work, sipping on some festive drinks, and feeling extra cuddly because (baby), it's cold outside...

### Notes

**[1]** I'd like to acknowledge these particular visualizations which inspired mine:
- [How common is your birthday? Find out exactly with an interactive heat map](https://public.tableau.com/app/profile/andy.kriebel/viz/MostCommonBirthdays/MostCommonBirthdays)
  by Andy Kriebel
- [How common is your birthday?](https://public.tableau.com/app/profile/karl3594/viz/HowCommonisYourBirthday_16298315732930/HowCommonisYourBirthday)
  by Karl Ericson
- [Birthday Dashboard](https://public.tableau.com/app/profile/datavizard/viz/BirthdayDashboard_0/BirthdayDashboard)
  by Jacob Olsufka

**[2]** I estimated conception dates by subtracting 280 days (the equivalent of 40 weeks) from the birthday dates.

