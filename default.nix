with import <nixpkgs> {};
stdenv.mkDerivation rec {
  name = "dev";
  env = buildEnv { name = name; paths = buildInputs; };
  buildInputs = [
    openjdk
    sbt
    nodejs-8_x
    yarn
    python36Packages.docker_compose
  ];
}
