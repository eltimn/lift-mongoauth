# This pins the packages to certain versions
with import (builtins.fetchTarball {
  # Descriptive name to make the store path easier to identify
  name = "nixos-20.09";
  # Commit hash for nixos as of 20.09
  url = "https://github.com/NixOS/nixpkgs/archive/20.09.tar.gz";
  # Hash obtained using `nix-prefetch-url --unpack <url>`
  sha256 = "1wg61h4gndm3vcprdcg7rc4s1v3jkm5xd7lw8r2f67w502y94gcy";
}) {};

stdenv.mkDerivation rec {
  name = "env";
  env = buildEnv { name = name; paths = buildInputs; };
  buildInputs = [
    jdk11
    sbt
    docker_compose
  ];
}
