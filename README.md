# Imani's personal site

## Development

### Cloning this repository to your local machine

Copy the HTTPS url from GitHub and open a terminal on your computer in the location you wish to store a working copy of
this *git repository* (think of it like a copy of all the files, plus information about 'branches' of their history).

![](./clone.png)

Then, run `git clone https://github.com/Imani-r/site.git` in the terminal. This downloads a copy of the *repo*.

### Running locally

Open (or navigate) a terminal to the root of this project on your machine (i.e. where this README is). Run this:

```shell
npx http-server

# first time only:
Need to install the following packages:
http-server@14.1.1
Ok to proceed? (y)
# press 'y' or Enter
```

You should then see something like:

```shell
Starting up http-server, serving ./

http-server version: 14.1.1

http-server settings:
CORS: disabled
Cache: 3600 seconds
Connection Timeout: 120 seconds
Directory Listings: visible
AutoIndex: visible
Serve GZIP Files: false
Serve Brotli Files: false
Default File Extension: none

Available on:
  http://127.0.0.1:8080
  http://192.168.1.118:8080
Hit CTRL-C to stop the server
```

This indicates there is a *local web server* running on your machine, serving the files of this repo at [localhost:8080](localhost:8080).
Go to this URL in your browser of choice to see what that looks like.

> [!NOTE]
> This is separate from the copy of the site that is served by [GitHub Pages](https://pages.github.com), and open to the
> public Internet. It is just a kind of 'preview' copy of things, for you to mess around with and see how changes *would*
> look once finally [published](#Publishing_posts).
>
> Go wild!

### Writing new posts

Open (or navigate) a terminal to the root of this project on your machine (i.e. where this README is). Run this:

```shell
dev/new.clj
# Usage: dev/new.clj <posts file name>
```

As the printed message indicates, you need to supply a file name for the new post. (This **does not** have to be the
same as the post's eventual title.) It may include spaces, but you should avoid punctuation. So for instance:

```shell
dev/new.clj Hello World
# dev/posts_content/Hello_World.html created
```

> [!NOTE]
> You may need to first install [babashka](https://github.com/babashka/babashka?tab=readme-ov-file#installation) for
> these scripts to work.

You may then open the new post (say, in [VSCode](https://code.visualstudio.com/)) and edit away!

> [!NOTE]
> Be careful when editing the 'Title Card', as a few things there are expected to be [well-formed](https://en.wikipedia.org/wiki/Well-formed_formula)
> in the sense that there is code in `dev/compile.clj` that depends on specific *ids* and *classnames*, and will
> automatically replace values in `{{curly braces}}` when the post is [published](#Publishing_posts).

### Editing post file names (and URLs)

Do not do this manually! Because `dev/db.edn` might get out of sync with the rest of the repo.

Instead, if you wish to edit an existing post's file name (and therefore its `/posts/<post_file_name>.html}` URL), from
a terminal at the root of this project, call:

```shell
dev/rename.clj <old name> : <new name>
```

### Publishing posts

There are two steps to publishing a finished `dev/posts_content` post:
  1. From a terminal at the root of this project, call `dev/compile.clj`. This will compile **all** `dev/posts_content/`
     posts that have changed since last calling `dev/compile.clj` to `posts/`; injecting appropriate `{{published}}` and
     `{{last-updated}}` dates; etc.
  2. *Committing* and *pushing* the compiled `posts/` to GitHub, so that [GitHub Pages](https://pages.github.com) can
     host them.

In the terminal, the latter is done like so. (But you may prefer to use [VSCode](https://code.visualstudio.com/)'s git
UI — which makes use of the same concepts.)

```shell
# 'Stage' the files you wish to publish, either naming them individually
git add posts/foo.html posts/bar.html ...
# Or all at once:
git add posts/

# Then, 'commit' the staged files with a message
git commit -m 'A couple new posts'

# And 'push' to GitHub
git push
```

And that's it! (Assuming you pushed to `main`) GitHub will 'build and deploy' your posts — which you can keep an eye on
[here](https://github.com/Imani-r/site/actions) — and they will be live.

Not too bad, eh?

## [ADVANCED:] `.clj` scripts development

### REPLing

```shell
cd dev
bb --config ../bb.edn nrepl-server 1667
```

(Bit of a weird solution, but otherwise either **(1)** the REPL cannot 'see' the `.clj` files — probably some classpath
issue? — or **(2)** calling them from the terminal, here at the root, is obnoxious — needing its own `--config` or so.)
