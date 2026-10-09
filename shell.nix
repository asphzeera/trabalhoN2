{ pkgs ? import <nixpkgs> {} }:

pkgs.mkShell {
  packages = with pkgs; [
    maven
    jdk21
    gtk3
    glib
    libx11
    libxxf86vm
    libxtst
    libGL
    fontconfig
    freetype
  ];

  shellHook = ''
    export LD_LIBRARY_PATH="${pkgs.lib.makeLibraryPath (with pkgs; [
      gtk3
      glib
      libx11
      libxxf86vm
      libxtst
      libGL
      fontconfig
      freetype
    ])}:$LD_LIBRARY_PATH"
  '';
}
