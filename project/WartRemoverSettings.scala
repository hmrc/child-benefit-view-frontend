import sbt.Compile
import sbt.Keys.compile
import wartremover.WartRemover.autoImport.{Wart, wartremoverErrors, wartremoverWarnings}

object  WartRemoverSettings {

  lazy val wartRemoverWarning = {
    val warningWarts = Seq(
      Wart.JavaSerializable,
      Wart.StringPlusAny,
      Wart.AsInstanceOf,
      Wart.IsInstanceOf,
      Wart.Any
    )
    wartremoverWarnings in(Compile, compile) ++= warningWarts
  }
  lazy val wartRemoverError = {
    // Error
    val errorWarts = Seq(
      Wart.ArrayEquals,
      Wart.AnyVal,
      Wart.EitherProjectionPartial,
      Wart.Enumeration,
      Wart.ExplicitImplicitTypes,
      Wart.FinalVal,
      Wart.JavaConversions,
      Wart.JavaSerializable,
      Wart.LeakingSealed,
      //Need mutable structures in relation to the Vars
      //Wart.MutableDataStructures,
      Wart.Null,
      Wart.OptionPartial,
      Wart.Recursion,
      Wart.Return,
      Wart.TraversableOps,
      Wart.TryPartial,
      //Its a bit dirty , but Vars are ok in stubs
      //Wart.Var,
      Wart.While)

    wartremoverErrors in(Compile, compile) ++= errorWarts
  }
}